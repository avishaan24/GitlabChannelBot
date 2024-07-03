package com.microsoftTeams.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.*;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import com.microsoftTeams.bot.helpers.*;
import com.microsoftTeams.bot.models.Merge;
import com.microsoftTeams.bot.models.MergeReference;
import com.microsoftTeams.bot.models.TeamChannel;
import com.microsoftTeams.bot.repository.MergeReferenceRepository;
import com.microsoftTeams.bot.repository.MergeRepository;
import com.microsoftTeams.bot.repository.TeamChannelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@SuppressWarnings("unused")
public class NotifyController {

    /**
     * The BotFrameworkHttpAdapter to use. Note is provided by dependency
     * injection via the constructor.
     *
     * @see com.microsoft.bot.integration.spring.BotDependencyConfiguration
     */
    private final BotFrameworkHttpAdapter adapter;

    private static final Logger logger = LoggerFactory.getLogger(NotifyController.class);

    @Autowired
    private TeamChannelRepository teamChannelRepository;

    @Autowired
    private MergeReferenceRepository mergeReferenceRepository;

    @Autowired
    private MergeRepository mergeRepository;

    private final String appId;
    private final String password;
    private final String accessToken;

    @Autowired
    public NotifyController(
            BotFrameworkHttpAdapter withAdapter,
            Environment environment,
            Configuration withConfiguration
    ) {
        adapter = withAdapter;
        appId = withConfiguration.getProperty("MicrosoftAppId");
        password = withConfiguration.getProperty("MicrosoftAppPassword");
        accessToken = environment.getProperty("accessToken");
    }

    /* sending pull request proactive message to the teams channel block started*/
    @PostMapping("/api/sendMessage")
    public void sendMsg(@RequestBody Information information) {
        // if the event is related to create, update and merge status of PR in gitlab
        switch (information.getObjectKind()) {
            case "merge_request":
                Merge merge = mergeRepository.findByMergeRequestId(information.getObjectAttributes().getId());
                if(merge == null){
                    merge = new Merge(information.getObjectAttributes().getId(), information.getProject().getName(),information.getObjectAttributes().getTitle(),  information.getObjectAttributes().getUrl(), information.getObjectAttributes().getLastCommit().getAuthor(), information.getObjectAttributes().getLastCommit().getId());
                    mergeRepository.save(merge);
                    // firstly check the status of the pipeline associated with the merge request
                    CompletableFuture<Pipeline> pipelineFuture = waitForPipelineStatus(information.getProject().getId(), information.getObjectAttributes().getHeadPipelineId());
                    AtomicReference<Pipeline> localPipeline = new AtomicReference<>(null); // Use AtomicReference
                    CompletableFuture<Void> resultHandler = pipelineFuture.thenAccept(pipeline -> {
                        if (pipeline != null) {
                            // Save the pipeline to the AtomicReference
                            localPipeline.set(pipeline);
                        } else {
                            System.out.println("Failed to receive Pipeline");
                        }
                    });

                    // wait for the resultHandler to ensure that the merge request handling is complete
                    resultHandler.join();

                    // use the localPipeline.get() to retrieve the pipeline
                    Pipeline pipeline = localPipeline.get();

                    // if it is success then check the merge Request is ready to merge or not
                    if(pipeline!= null && pipeline.getStatus().equals("success")){
                        List<TeamChannel> teamChannels1 = teamChannelRepository.findByMembersEmail(information.getObjectAttributes().getLastCommit().getAuthor().getEmail());
                        String messagePipeline = " on the project \"" + information.getProject().getName() + "\" with title \"" + information.getObjectAttributes().getTitle() +"\" \n \n PR link: " + information.getObjectAttributes().getUrl() + "\n \n Pipeline passed for the pull request";
                        sendNotification(teamChannels1, messagePipeline, information.getObjectAttributes().getId(), information.getObjectAttributes().getLastCommit().getAuthor()).join();
                    }
                    break;
                }
                List<MergeReference> mergeReferenceList = mergeReferenceRepository.findByMergeRequestId(information.getObjectAttributes().getId());
                MergeReference mergeReference = null;
                if (!mergeReferenceList.isEmpty()) {
                    mergeReference = mergeReferenceList.get(0);
                }
                List<TeamChannel> teamChannels;
                String message = "";
                switch (information.getObjectAttributes().getState()) {
                    case "merged":
                        message = message + "Merged by " + information.getUser().getName();
                        break;
                    // notify user when PR is closed
                    case "closed":
                        message = message + "Closed by " + information.getUser().getName();
                        break;
                    // notify user when some other user commit in the PR
                    case "opened":
                        if(!information.getObjectAttributes().getLastCommit().getId().equals(merge.getLastCommitId())){
                            merge.setLastCommitId(information.getObjectAttributes().getLastCommit().getId());
                            mergeRepository.save(merge);
                            message =  "\n A new commit added by " + information.getUser().getName() + ", with the message " + information.getObjectAttributes().getLastCommit().getMessage();
                        }
                        else if(information.getObjectAttributes().getDetailedMergeStatus().equals("mergeable") || information.getObjectAttributes().getAction().equals("approved")){
                            message = "Pull request approved and ready to merge";
                        }
                        break;
                }
                if(message.isEmpty()){
                    break;
                }
                if (mergeReference != null) {
                    teamChannels = teamChannelRepository.findByMembersEmail(mergeReference.getAuthor().getEmail());
                    sendNotification(teamChannels, message, information.getObjectAttributes().getId(), information.getObjectAttributes().getLastCommit().getAuthor()).join();
                }
                break;
            // if the event type is comment
            case "note":
                // when comment is made on the PR notify the author of the PR
                if (information.getObjectAttributes().getNoteableType().equals("MergeRequest")) {
                    List<MergeReference> mergeReferenceListNote = mergeReferenceRepository.findByMergeRequestId(information.getObjectAttributes().getNoteableId());
                    MergeReference mergeReferenceNote = null;
                    if (!mergeReferenceListNote.isEmpty()) {
                        mergeReferenceNote = mergeReferenceListNote.get(0);
                    }
                    if(mergeReferenceNote != null){
                        List<TeamChannel> teamChannelsNote = teamChannelRepository.findByMembersEmail(mergeReferenceNote.getAuthor().getEmail());
                        String messageNote = "New comments added by " + information.getUser().getName() + "\n" + "\nComment : " + information.getObjectAttributes().getNote();
                        sendNotification(teamChannelsNote, messageNote, information.getObjectAttributes().getNoteableId(), null).join();
                    }
                }
                break;
            // if the event is related to the pipeline
            case "pipeline":
                CompletableFuture<MergeRequest> mergeRequestFuture;
                AtomicReference<MergeRequest> localMergeRequest;
                CompletableFuture<Void> resultHandler;
                MergeRequest mergeRequest;

                // notify user when pipeline is failed
                switch (information.getObjectAttributes().getStatus()) {
                    case "failed":
                        mergeRequestFuture = waitForMergeRequest(information.getProject().getId(), information.getObjectAttributes().getSha());
                        localMergeRequest = new AtomicReference<>(null); // Use AtomicReference
                        resultHandler = mergeRequestFuture.thenAccept(mergeRequestDetails -> {
                            if (mergeRequestDetails != null) {
                                // Save the merge request to the AtomicReference
                                localMergeRequest.set(mergeRequestDetails);
                            } else {
                                System.out.println("Failed to receive Merge Request");
                            }
                        });
                        resultHandler.join();
                        mergeRequest = localMergeRequest.get();
                        if(mergeRequest != null){
                            for (Builds build : information.getBuilds()) {
                                if (build.getStatus().equals("failed")) {
                                    Merge mergePipeline = mergeRepository.findByMergeRequestId(mergeRequest.getId());
                                    List<MergeReference> mergeRequestList = mergeReferenceRepository.findByMergeRequestId(mergeRequest.getId());
                                    MergeReference mergeReference1 = null;
                                    if (!mergeRequestList.isEmpty()) {
                                        mergeReference1 = mergeRequestList.get(0);
                                    }
                                    List<TeamChannel> teamChannels1;
                                    if(mergeReference1 != null){
                                        teamChannels1 = teamChannelRepository.findByMembersEmail(mergePipeline.getAuthor().getEmail());
                                        String messagePipeline = "Pipeline failed at " + build.getStage() + " stage";
                                        sendNotification(teamChannels1, messagePipeline, mergeRequest.getId(), mergePipeline.getAuthor()).join();
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    case "success":
                        mergeRequestFuture = waitForMergeRequest(information.getProject().getId(), information.getObjectAttributes().getSha());
                        localMergeRequest = new AtomicReference<>(null); // Use AtomicReference
                        resultHandler = mergeRequestFuture.thenAccept(mergeRequests -> {
                            if (mergeRequests != null) {
                                // Save the merge request to the AtomicReference
                                localMergeRequest.set(mergeRequests);
                            } else {
                                System.out.println("Failed to receive Merge Request");
                            }
                        });
                        resultHandler.join();
                        mergeRequest = localMergeRequest.get();

                        if (mergeRequest != null) {
                            Merge mergePipeline = mergeRepository.findByMergeRequestId(mergeRequest.getId());
                            List<MergeReference> mergeRequestList = mergeReferenceRepository.findByMergeRequestId(mergeRequest.getId());
                            MergeReference mergeReference1 = null;
                            if (!mergeRequestList.isEmpty()) {
                                mergeReference1 = mergeRequestList.get(0);
                            }
                            List<TeamChannel> teamChannels1;
                            teamChannels1 = teamChannelRepository.findByMembersEmail(mergePipeline.getAuthor().getEmail());
                            if(mergeReference1 == null){
                                String messagePipeline = " on the project \"" + mergePipeline.getProjectName() + "\" with title \"" + mergePipeline.getTitle() +"\" \n \n PR link: " + mergePipeline.getUrl() + "\n";
                                if(mergeRequest.getMergeStatus().equals("can_be_merged") && mergeRequest.getHasConflicts().equals("false") && mergeRequest.getState().equals("opened")){
                                    if(mergeRequest.getDetailedMergeStatus().equals("mergeable")){
                                        messagePipeline = messagePipeline + "\n Pipeline passed,  pull request is ready to merge";
                                    }
                                    else{
                                        messagePipeline = messagePipeline + "\n Pipeline passed,  waiting for approvals";
                                    }
                                    sendNotification(teamChannels1, messagePipeline, mergeRequest.getId(), mergePipeline.getAuthor()).join();
                                }
                            }
                            else{
                                if(mergeRequest.getMergeStatus().equals("can_be_merged") && mergeRequest.getHasConflicts().equals("false") && mergeRequest.getState().equals("opened")){
                                    String messagePipeline = "Pipeline passed,  waiting for approvals";
                                    if(mergeRequest.getDetailedMergeStatus().equals("mergeable")){
                                        messagePipeline = "Pipeline passed,  pull request is ready to merge";
                                    }
                                    sendNotification(teamChannels1, messagePipeline, mergeRequest.getId(), mergeRequest.getAuthor()).join();
                                }
                            }
                        }
                        break;
                }
                break;
        }
    }

    private CompletableFuture<Void> sendNotification(List<TeamChannel> teamChannels, String message, String mergeRequestId, Author author){
        return CompletableFuture.runAsync(() -> {
            for (TeamChannel teamChannel : teamChannels) {
                MergeReference conversation = mergeReferenceRepository.findByMergeRequestIdAndTeamsChannelId(mergeRequestId, teamChannel.getTeamsChannelId());
                MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(appId, password);
                if(conversation == null) {
                    Mention mention = getMention(author, teamChannel);
                    Activity attachment = MessageFactory.text("New Pull request created by " + mention.getText() + " " + message);
                    attachment.setMentions(Collections.singletonList(mention));

                    ObjectNode channelData = JsonNodeFactory.instance.objectNode();
                    channelData.set("channel", JsonNodeFactory.instance.objectNode()
                                    .set("id", JsonNodeFactory.instance.textNode(teamChannel.getTeamsChannelId())));

                    ConversationParameters conversationParameters = new ConversationParameters();
                    conversationParameters.setIsGroup(true);
                    conversationParameters.setActivity(attachment);
                    conversationParameters.setChannelData(channelData);

                    adapter.createConversation(teamChannel.getTeamsChannelId(), teamChannel.getServiceUrl(), credentials, conversationParameters,
                            tc -> {
                                ConversationReference reference = tc.getActivity().getConversationReference();
                                ObjectMapper mapper = new ObjectMapper();
                                String json = "";
                                try {
                                    // Serialize to JSON string
                                    json = mapper.writeValueAsString(reference);
                                } catch (JsonProcessingException e) {
                                    logger.error("A JsonProcessingException occurred : {}", e.getMessage(), e); // Handle the exception according to your application's error handling strategy
                                }
                                try {
                                    // Create MergeRequest object
                                    MergeReference mergeRequestAdd = new MergeReference(mergeRequestId, author, teamChannel.getTeamsChannelId(), json);
                                    mergeReferenceRepository.save(mergeRequestAdd);
                                } catch (Exception e) {
                                    System.err.println("Error saving merge request: " + e.getMessage());
                                    logger.error("An Exception occurred : {}", e.getMessage(), e);
                                }
                                return CompletableFuture.completedFuture(null);
                            });

                } else {
                    Activity attachment = MessageFactory.text(message);
                    ConversationReference checking = null;
                    ObjectMapper objectMapper = new ObjectMapper();
                    try{
                        // Deserialize from JSON string (just for illustration, not necessary here)
                        checking = objectMapper.readValue(conversation.getConversationReference(), ConversationReference.class);
                    }catch (JsonProcessingException e){
                        logger.error("A JsonProcessingException occurred : {}", e.getMessage(), e);
                    }
                    adapter.continueConversation(appId, checking,
                            (continue_tc) -> continue_tc.sendActivity(attachment)
                                    .thenCompose(resourceResponse -> CompletableFuture.completedFuture(null)));
                }
            }
        });
    }

    private Mention getMention(Author author, TeamChannel teamChannel) {
        TeamsChannelAccount user = null;
        for(TeamsChannelAccount teamsChannelAccount : teamChannel.getMembers()){
            if(teamsChannelAccount.getEmail().equals(author.getEmail())){
                user = teamsChannelAccount;
                break;
            }
        }
        Mention mention = new Mention();
        if(user != null){
            mention.setMentioned(user);
            mention.setText("<at>" + user.getName() +  "</at>");
        }
        return mention;
    }

    /**
     * Fetch merge request related to the pipeline
     * @param projectId project id of the merge Request
     * @param sha sha id of the commit
     * @return mergeRequest
     */
    private CompletableFuture<MergeRequest> waitForMergeRequest(String projectId, String sha) {
        String url = "https://gitlab.com/api/v4/projects/" + projectId + "/repository/commits/" + sha + "/merge_requests";
        return fetchMergeRequest(url);
    }

    /**
     * Fetch pipeline from the Gitlab API
     * @param projectId project id of the merge Request
     * @param headPipelineId head Pipeline id of the merge request
     * @return pipeline
     */
    private CompletableFuture<Pipeline> waitForPipelineStatus(String projectId, String headPipelineId){
        String url = "https://gitlab.com/api/v4/projects/" + projectId + "/pipelines/" + headPipelineId;
        return fetchPipeline(url);
    }

    /**
     * call gitlab api to fetch merge request
     * @param url endpoint url
     * @return merge Request details from the API
     */
    private CompletableFuture<MergeRequest> fetchMergeRequest(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("PRIVATE-TOKEN", accessToken);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    // Read the response into a JsonNode
                    JsonNode rootNode = objectMapper.readTree(connection.getInputStream());

                    // Check if the root node is an array
                    if (rootNode.isArray() && !rootNode.isEmpty()) {
                        // Get the first element from the array
                        JsonNode mergeRequestNode = rootNode.get(0);
                        // Deserialize the JSON object into a MergeRequest object
                        return objectMapper.treeToValue(mergeRequestNode, MergeRequest.class);
                    } else {
                        return null;
                    }
                } else {
                    System.err.println("Error: " + connection.getResponseMessage());
                    return null; // Return null if there was an error
                }
            } catch (IOException e) {
                logger.error("An IOException occurred : {}", e.getMessage(), e);
                return null; // Return null in case of exception
            }
        });
    }

    /**
     * call gitlab api to fetch pipeline
     * @param url endpoint url
     * @return pipeline details
     */
    private CompletableFuture<Pipeline> fetchPipeline(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("PRIVATE-TOKEN", accessToken);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    // Read the response into a JsonNode
                    JsonNode rootNode = objectMapper.readTree(connection.getInputStream());

                    // Deserialize the JSON object into a MergeRequest object
                    return objectMapper.treeToValue(rootNode, Pipeline.class);
                } else {
                    System.err.println("Error: " + connection.getResponseMessage());
                    return null; // Return null if there was an error
                }
            } catch (IOException e) {
                logger.error("An IOException occurred : {}", e.getMessage(), e);
                return null; // Return null in case of exception
            }
        });
    }

    /* block end*/

}