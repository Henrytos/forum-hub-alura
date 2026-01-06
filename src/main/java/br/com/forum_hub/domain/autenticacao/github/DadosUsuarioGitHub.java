package br.com.forum_hub.domain.autenticacao.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DadosUsuarioGitHub(String login, Long id,
                                 @JsonProperty("node_id") String nodeId,
                                 @JsonProperty("avatar_url") String avatarUrl,
                                 @JsonProperty("gravatar_id") String gravatarId, String url,
                                 @JsonProperty("html_url") String htmlUrl,
                                 @JsonProperty("followers_url") String followersUrl,
                                 @JsonProperty("following_url") String followingUrl,
                                 @JsonProperty("gists_url") String gistsUrl,
                                 @JsonProperty("starred_url") String starredUrl,
                                 @JsonProperty("subscriptions_url") String subscriptionsUrl,
                                 @JsonProperty("organizations_url") String organizationsUrl,
                                 @JsonProperty("repos_url") String reposUrl,
                                 @JsonProperty("events_url") String eventsUrl,
                                 @JsonProperty("received_events_url") String receivedEventsUrl, String type,
                                 @JsonProperty("user_view_type") String userViewType,
                                 @JsonProperty("site_admin") boolean siteAdmin, String name, String company,
                                 String blog, String location, String email, Boolean hireable, String bio,
                                 @JsonProperty("twitter_username") String twitterUsername,
                                 @JsonProperty("notification_email") String notificationEmail,
                                 @JsonProperty("public_repos") Integer publicRepos,
                                 @JsonProperty("public_gists") Integer publicGists, Integer followers,
                                 Integer following, @JsonProperty("created_at") String createdAt,
                                 @JsonProperty("updated_at") String updatedAt,
                                 @JsonProperty("private_gists") Integer privateGists,
                                 @JsonProperty("total_private_repos") Integer totalPrivateRepos,
                                 @JsonProperty("owned_private_repos") Integer ownedPrivateRepos,
                                 @JsonProperty("disk_usage") Long diskUsage, Integer collaborators,
                                 @JsonProperty("two_factor_authentication") boolean twoFactorAuthentication,
                                 PlanoGitHub plan) {
    public record PlanoGitHub(String name, Long space, Integer collaborators,
                              @JsonProperty("private_repos") Integer privateRepos) {
    }
}