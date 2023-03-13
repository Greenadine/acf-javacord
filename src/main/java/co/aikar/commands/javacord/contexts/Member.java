/*
 * Copyright (c) 2022 Kevin Zuman (Greenadine)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package co.aikar.commands.javacord.contexts;

import co.aikar.commands.javacord.exception.UserNoMemberOfServerException;
import com.google.common.base.Preconditions;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordClient;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserFlag;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeOverwrittenPermissionsListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;
import org.javacord.api.listener.channel.user.PrivateChannelCreateListener;
import org.javacord.api.listener.channel.user.PrivateChannelDeleteListener;
import org.javacord.api.listener.interaction.*;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageReplyListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.listener.server.member.ServerMemberBanListener;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import org.javacord.api.listener.server.member.ServerMemberUnbanListener;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;
import org.javacord.api.listener.user.*;
import org.javacord.api.util.event.ListenerManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a {@link User} within the context of a {@link Server}.
 *
 * @since 0.1
 * @author Greenadine
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class Member implements User {

    private final User user;
    private final Server server;

    public Member(@NotNull User user, @NotNull Server server) {
        if (!server.isMember(user)) {
            throw new UserNoMemberOfServerException(user, server);
        }
        this.user = user;
        this.server = server;
    }
    
    /**
     * Gets the {@link Server} where the member is present.
     *
     * @return the member's {@link Server}.
     */
    public Server getServer() {
        return server;
    }

    /**
     * Gets the display name of the member.
     * <p>Gets the nickname of the member if present, otherwise the member's username.</p>
     *
     * @return the member's display name.
     *
     * @see Member#getNickname()
     * @see Member#getName()
     */
    public String getDisplayName() {
        return getNickname().orElse(getName());
    }

    /**
     * Gets the server-specific avatar of the member.
     *
     * @return the member's server-specific avatar.
     */
    public Optional<Icon> getServerAvatar() {
        return user.getServerAvatar(server);
    }

    /**
     * Gets the server-specific avatar of the member at the given image size.
     *
     * @param i the size of the image, must be any power of 2 between 16 and 4096.
     *
     * @return the member's server-specific avatar at the given image size.
     */
    public Optional<Icon> getServerAvatar(int i) {
        return user.getServerAvatar(server, i);
    }

    /**
     * Gets the effective avatar of the member. This will return the member's server-specific avatar if they have one,
     * otherwise it will return their account avatar.
     *
     * @return the member's effective avatar.
     */
    public Icon getEffectiveAvatar() {
        return user.getEffectiveAvatar(server);
    }

    /**
     * Gets the effective avatar of the member at the given image size. This will return the member's server-specific
     * avatar if they have one, otherwise it will return their account avatar.
     *
     * @param i the size of the image, must be any power of 2 between 16 and 4096.
     *
     * @return the member's effective avatar at the given image size.
     */
    public Icon getEffectiveAvatar(int i) {
        return user.getEffectiveAvatar(server, i);
    }

    /**
     * Gets the timestamp of when the member joined the server.
     *
     * @return the {@link Instant} timestamp of when the member joined the server.
     *
     * @see Server#getJoinedAtTimestamp(User)
     */
    public Instant getJoinedAtTimestamp() {
        return server.getJoinedAtTimestamp(user).get();
    }

    /**
     * Gets the {@link Instant} of when the {@link User} started boosting the {@link Server}.
     *
     * @return the {@code Instant} of when the {@code User} started boosting the {@code Server}.
     */
    public Optional<Instant> getServerBoostingSinceTimestamp() {
        return server.getServerBoostingSinceTimestamp(user);
    }

    /**
     * Get the hash of the {@link User}s server avatar.
     *
     * @return the {@code User}s server avatar hash.
     */
    public Optional<String> getServerAvatarHash() {
        return server.getUserServerAvatarHash(user);
    }

    /**
     * Gets the nickname of the member, if present.
     *
     * @return the member's nickname.
     *
     * @see Server#getNickname(User)
     */
    public Optional<String> getNickname() {
        return server.getNickname(user);
    }

    /**
     * Checks whether the member has a nickname set within the server.
     *
     * @return {@code true} if the member has a nickname, {@code false} otherwise.
     */
    public boolean hasNickname() {
        return getNickname().isPresent();
    }

    /**
     * Changes the nickname of the member.
     *
     * @param nickname the new nickname of the member.
     *
     * @return A {@code Future} to check if the update was successful.
     *
     * @see Server#updateNickname(User, String)
     */
    public CompletableFuture<Void> updateNickname(String nickname) {
        return server.updateNickname(user, nickname);
    }

    /**
     * Changes the nickname of the member.
     *
     * @param nickname the new nickname of the member.
     * @param reason the audit log reason for this update.
     *
     * @return A {@code Future} to check if the update was successful.
     *
     * @see Server#updateNickname(User, String)
     */
    public CompletableFuture<Void> updateNickname(String nickname, String reason) {
        return server.updateNickname(user, nickname, reason);
    }

    /**
     * Removes the nickname of the member.
     *
     * @return A {@code Future} to check if the update was successful.
     *
     * @see Server#resetNickname(User)
     */
    public CompletableFuture<Void> resetNickname() {
        return server.resetNickname(user);
    }

    /**
     * Removes the nickname of the member.
     *
     * @param reason the audit log reason for this update.
     *
     * @return A {@code Future} to check if the update was successful.
     *
     * @see Server#resetNickname(User, String)
     */
    public CompletableFuture<Void> resetNickname(String reason) {
        return server.resetNickname(user, reason);
    }

    /**
     * Gets a sorted list (by position) with all roles of the member within the server.
     *
     * @return a sorted list (by position) with all roles of the member within the server.
     *
     * @see Server#getRoles(User)
     */
    public List<Role> getRoles() {
        return server.getRoles(user);
    }

    /**
     * Adds the given role to the member.
     *
     * @param role the role which should be added to the member.
     *
     * @return A {@code Future} to check if the update was successful.
     *
     * @see Server#addRoleToUser(User, Role)
     */
    public CompletableFuture<Void> addRole(Role role) {
        return server.addRoleToUser(user, role);
    }

    /**
     * Adds the given role to the member.
     *
     * @param role the role which should be added to the member.
     * @param reason the audit log reason for this update.
     *
     * @return A {@code Future} to check if the update was successful.
     *
     * @see Server#addRoleToUser(User, Role, String)
     */
    public CompletableFuture<Void> addRole(Role role, String reason) {
        return server.addRoleToUser(user, role, reason);
    }

    /**
     * Removes the given role from the member.
     *
     * @param role the role which should be removed from the member.
     *
     * @return A {@code Future} to check if the update was successful.
     *
     * @see Server#removeRoleFromUser(User, Role)
     */
    public CompletableFuture<Void> removeRole(Role role) {
        return server.removeRoleFromUser(user, role);
    }

    /**
     * Removes the given role from the member.
     *
     * @param role the role which should be removed from the member.
     * @param reason the audit log reason for this update.
     *
     * @return A {@code Future} to check if the update was successful.
     *
     * @see Server#removeRoleFromUser(User, Role, String)
     */
    public CompletableFuture<Void> removeRole(Role role, String reason) {
        return server.removeRoleFromUser(user, role, reason);
    }

    /**
     * Updates the roles of the member.
     * This will replace the roles of the member with a provided collection.
     *
     * @param roles the collection of roles to replace the member's roles.
     *
     * @return A {@code Future} to check if the update was successful.
     *
     * @see Server#updateRoles(User, Collection)
     */
    public CompletableFuture<Void> updateRoles(Collection<Role> roles) {
        return server.updateRoles(user, roles);
    }

    /**
     * Updates the roles of the member.
     * This will replace the roles of the member with a provided collection.
     *
     * @param roles the collection of roles to replace the member's roles.
     * @param reason the audit log reason for this update.
     *
     * @return A {@code Future} to check if the update was successful.
     *
     * @see Server#updateRoles(User, Collection, String)
     */
    public CompletableFuture<Void> updateRoles(Collection<Role> roles, String reason) {
        return server.updateRoles(user, roles, reason);
    }

    /**
     * Gets the displayed color of the member on their roles on the server.
     *
     * @return the color.
     *
     * @see Server#getRoleColor(User)
     */
    public Optional<Color> getRoleColor() {
        return server.getRoleColor(user);
    }

    /**
     * Returns whether the member is connected to a voice channel within the server.
     *
     * @return {@code true} if the member is connected to a server's voice channel, {@code false} otherwise.
     */
    public boolean isInVoiceChannel() {
        return server.getVoiceChannels().stream().anyMatch(user::isConnected);
    }

    /**
     * Returns whether the member is connected to the given voice channel within the server.
     *
     * @param channel the {@link ServerVoiceChannel}.
     *
     * @return {@code true} if the member is connected to the given voice channel, {@code false} otherwise.
     */
    public boolean isConnected(ServerVoiceChannel channel) {
        return user.isConnected(channel);
    }

    /**
     * Moves the member to the given channel on the server.
     *
     * @param channel the channel to move the member to.
     *
     * @return A {@code Future} to check if the move was successful.
     *
     * @see Server#moveUser(User, ServerVoiceChannel)
     */
    public CompletableFuture<Void> moveToVoiceChannel(ServerVoiceChannel channel) {
        return server.moveUser(user, channel);
    }

    /**
     * Kicks the member from any voice channel.
     *
     * @return A {@code Future} to check if the kick was successful.
     *
     * @see Server#kickUserFromVoiceChannel(User)
     */
    public CompletableFuture<Void> kickFromVoiceChannel() {
        return server.kickUserFromVoiceChannel(user);
    }

    /**
     * Mutes the member on the server.
     *
     * @return A {@code Future} to check if the mute was successful.
     *
     * @see Server#muteUser(User)
     */
    public CompletableFuture<Void> mute() {
        return server.muteUser(user);
    }

    /**
     * Mutes the member on the server.
     *
     * @param reason the audit log reason for this action.
     *
     * @return A {@code Future} to check if the mute was successful.
     *
     * @see Server#muteUser(User, String)
     */
    public CompletableFuture<Void> mute(String reason) {
        return server.muteUser(user, reason);
    }

    /**
     * Unmutes the member on the server.
     *
     * @return A {@code Future} to check if unmute was successful.
     *
     * @see Server#unmuteUser(User)
     */
    public CompletableFuture<Void> unmute() {
        return server.unmuteUser(user);
    }

    /**
     * Unmutes the member on the server.
     *
     * @param reason the audit log reason for this action.
     *
     * @return A {@code Future} to check if unmute was successful.
     */
    public CompletableFuture<Void> unmute(String reason) {
        return server.unmuteUser(user, reason);
    }

    /**
     * Checks whether the member is server muted (muted by a moderator) within the server.
     *
     * @return {@code true} if the member is server muted, {@code false} otherwise.
     *
     * @see Server#isMuted(User)
     */
    public boolean isMuted() {
        return server.isMuted(user);
    }

    /**
     * Checks whether the member has muted themselves within the server.
     *
     * @return {@code true} if the member has muted themselves, {@code false} otherwise.
     *
     * @see Server#isSelfMuted(User)
     */
    public boolean isSelfMuted() {
        return server.isSelfMuted(user);
    }

    /**
     * Deafens the member on the server.
     *
     * @return A {@code Future} to check if deafen was successful.
     *
     * @see Server#deafenUser(User)
     */
    public CompletableFuture<Void> deafen() {
        return server.deafenUser(user);
    }

    /**
     * Deafens the member on the server.
     *
     * @param reason the audit log reason for this action.
     *
     * @return A {@code Future} to check if deafen was successful.
     *
     * @see Server#deafenUser(User, String)
     */
    public CompletableFuture<Void> deafen(String reason) {
        return server.deafenUser(user, reason);
    }

    /**
     * Undeafens the member on the server.
     *
     * @return A {@code Future} to check if the undeafen was successful.
     *
     * @see Server#undeafenUser(User)
     */
    public CompletableFuture<Void> undeafen() {
        return server.undeafenUser(user);
    }

    /**
     * Undeafens the member on the server.
     *
     * @param reason the audit log reason for this action.
     *
     * @return A {@code Future} to check if the undeafen was successful.
     *
     * @see Server#undeafenUser(User, String)
     */
    public CompletableFuture<Void> undeafen(String reason) {
        return server.undeafenUser(user, reason);
    }

    /**
     * Checks whether the member is server deafened (deafened by a moderator) within the server.
     *
     * @return {@code true} if the member is server deafened, {@code false} otherwise.
     *
     * @see Server#isDeafened(User)
     */
    public boolean isDeafened() {
        return server.isDeafened(user);
    }

    /**
     * Checks whether the member has deafened themselves within the server.
     *
     * @return {@code true} if the member has deafened themselves, {@code false} otherwise.
     *
     * @see Server#isSelfDeafened(User)
     */
    public boolean isSelfDeafened() {
        return server.isSelfDeafened(user);
    }

    /**
     * Timeouts the member on the server until the given {@link Instant} timestamp.
     *
     * @param timeout The {@code Instant}.
     *
     * @return A {@code Future} to check if the timeout was successful.
     */
    public CompletableFuture<Void> timeout(Instant timeout) {
        return user.timeout(server, timeout);
    }

    /**
     * Timeouts the member on the server for the given {@link Duration}.
     * 
     * @param duration The {@code Duration}.
     *                 
     * @return A {@code Future} to check if the timeout was successful.
     */
    public CompletableFuture<Void> timeout(Duration duration) {
        return user.timeout(server, duration);
    }

    /**
     * Timeouts the member on the server until the given {@link Instant} timestamp for the provided reason.
     *
     * @param timeout The {@code Instant}.
     * @param reason A description of the reason for the timeout.
     *
     * @return A {@code Future} to check if the timeout was successful.
     */
    public CompletableFuture<Void> timeout(Instant timeout, String reason) {
        return user.timeout(server, timeout, reason);
    }

    /**
     * Timeouts the member on the server for the given {@link Duration} for the provided reason.
     *
     * @param duration The {@code Duration}.
     * @param reason A description of the reason for the timeout.
     *
     * @return A {@code Future} to check if the timeout was successful.
     */
    public CompletableFuture<Void> timeout(Duration duration, String reason) {
        return user.timeout(server, duration, reason);
    }

    /**
     * Gets the timestamp of when the member's timeout will expire, and the member will be able to communicate in the server again.
     *
     * @return An {@link Instant} of when the member's timeout will expire.
     */
    public Optional<Instant> getTimeout() {
        return user.getTimeout(server);
    }

    /**
     * Gets the timestamp of when the member's timeout will expire, and the member will be able to communicate in the server again.
     *
     * @return An {@link Instant} of when the member's timeout will expire.
     */
    public Optional<Instant> getActiveTimeout() {
        return user.getActiveTimeout(server);
    }

    /**
     * Removes a timeout of the member on the server.
     *
     * @return A {@code Future} to check if the timeout removal was successful.
     */
    public CompletableFuture<Void> removeTimeout() {
        return user.removeTimeout(server);
    }

    /**
     * Removes a timeout of the member on the server with the provided reason.
     *
     * @param reason A description of the reason for the timeout removal.
     *
     * @return A {@code Future} to check if the timeout removal was successful.
     */
    public CompletableFuture<Void> removeTimeout(String reason) {
        return user.removeTimeout(server, reason);
    }

    /**
     * Kicks the member from the server.
     *
     * @return A {@code Future} to check if the kick was successful.
     *
     * @see Server#kickUser(User)
     */
    public CompletableFuture<Void> kick() {
        return server.kickUser(user);
    }

    /**
     * Kicks the member from the server.
     *
     * @param reason the audit log reason for this action.
     *
     * @return A {@code Future} to check if the kick was successful.
     *
     * @see Server#kickUser(User, String)
     */
    public CompletableFuture<Void> kick(String reason) {
        return server.kickUser(user, reason);
    }

    /**
     * Checks whether the member is banned from the server.
     *
     * @return {@code true} if the member is banned, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> isBanned() {
        return server.getBans().thenApply(bans -> bans.stream().anyMatch(ban -> ban.getUser().getId() == getId()));
    }

    /**
     * Bans the member from the server.
     *
     * @see Server#banUser(User)
     */
    public CompletableFuture<Void> ban() {
        return server.banUser(user);
    }

    /**
     * Bans the member from the server.
     *
     * @param duration the duration in which to delete the messages for.
     *
     * @return A {@code Future} to check if the ban was successful.
     *
     * @see Server#banUser(User, Duration)
     */
    public CompletableFuture<Void> ban(Duration duration) {
        return server.banUser(user, duration);
    }

    /**
     * Bans the member from the server.
     *
     * @param duration the duration in which to delete the messages for.
     * @param reason the audit log reason for this action.
     *
     * @return A {@code Future} to check if the ban was successful.
     *
     * @see Server#banUser(User, Duration, String)
     */
    public CompletableFuture<Void> ban(Duration duration, String reason) {
        return server.banUser(user, duration, reason);
    }

    /**
     * Unbans the member from the server.
     *
     * @return A {@code Future} to check if the unban was successful.
     *
     * @see Server#unbanUser(User)
     */
    public CompletableFuture<Void> unban() {
        return server.unbanUser(user);
    }

    /**
     * Unbans the member from the server.
     *
     * @param reason the audit log reason for this action.
     *
     * @return A {@code Future} to check if the unban was successful.
     *
     * @see Server#unbanUser(User, String)
     */
    public CompletableFuture<Void> unban(String reason) {
        return server.unbanUser(user, reason);
    }

    /**
     * Gets the permissions of the member.
     *
     * @return the permissions of the member.
     *
     * @see Server#getPermissions(User)
     */
    public Permissions getPermissions() {
        return server.getPermissions(user);
    }

    /**
     * Get the allowed permissions of the member.
     *
     * @return the allowed permissions of the given user.
     *
     * @see Server#getAllowedPermissions(User)
     */
    public Collection<PermissionType> getAllowedPermissions() {
        return server.getAllowedPermissions(user);
    }

    /**
     * Gets the unset permissions of the member.
     *
     * @return the unset permissions of the member.
     *
     * @see Server#getUnsetPermissions(User)
     */
    public Collection<PermissionType> getUnsetPermissions() {
        return server.getUnsetPermissions(user);
    }

    /**
     * Checks the member has a given set of permissions.
     *
     * @param type the permission type(s) to check.
     *
     * @return {@code true} if the member has all the given permissions, {@code false} otherwise.
     *
     * @see Server#hasPermissions(User, PermissionType...)
     */
    public boolean hasPermissions(PermissionType... type) {
        return server.hasPermissions(user, type);
    }

    /**
     * Checks if the member has any of given set of permissions.
     *
     * @param type the permission type(s) to check.
     *
     * @return {@code true} if the member has any of the given permissions, {@code false} otherwise.
     *
     * @see Server#hasAnyPermission(User, PermissionType...)
     */
    public boolean hasAnyPermission(PermissionType... type) {
        return server.hasAnyPermission(user, type);
    }

    /**
     * Gets the voice channel the member is connected to, if any.
     *
     * @return the voice channel the member is connected to.
     *
     * @see Server#getConnectedVoiceChannel(User)
     */
    public Optional<ServerVoiceChannel> getConnectedVoiceChannel() {
        return server.getConnectedVoiceChannel(user);
    }

    /**
     * Gets a sorted (by position) list with all channels of the server the member can see.
     *
     * @return the visible channels of the server.
     *
     * @see Server#getVisibleChannels(User)
     */
    public List<ServerChannel> getVisibleChannels() {
        return server.getVisibleChannels(user);
    }

    /**
     * Gets the highest role of the member in the server, if present.
     *
     * @return the highest role of the member.
     */
    public Optional<Role> getHighestRole() {
        return server.getHighestRole(user);
    }

    /**
     * Checks whether the member is the owner of the server.
     *
     * @return {@code true} if the member is the owner, {@code false} otherwise.
     *
     * @see Server#isOwner(User)
     */
    public boolean isOwner() {
        return server.isOwner(user);
    }

    /**
     * Checks whether the member is an administrator of the server.
     *
     * @return {@code true} if the member is an administrator, {@code false} otherwise.
     *
     * @see Server#isAdmin(User)
     */
    public boolean isAdmin() {
        return server.isAdmin(user);
    }

    /**
     * Checks if the member can create new channels.
     *
     * @return {@code true} if the member can create channels, {@code false} otherwise.
     *
     * @see Server#canCreateChannels(User)
     */
    public boolean canCreateChannels() {
        return server.canCreateChannels(user);
    }

    /**
     * Checks if the member can view the audit log of the server.
     *
     * @return {@code true} if the member can view the audit log, {@code false} otherwise.
     *
     * @see Server#canViewAuditLog(User)
     */
    public boolean canViewAuditLog() {
        return server.canViewAuditLog(user);
    }

    /**
     * Checks if the member can change their own nickname in the server.
     *
     * @return {@code true} if the member can change their own nickname, {@code false} otherwise.
     *
     * @see Server#canChangeOwnNickname(User)
     */
    public boolean canChangeOwnNickname() {
        return server.canChangeOwnNickname(user);
    }

    /**
     * Checks if the member can change manage nicknames on the server.
     *
     * @return {@code true} if the member can manage nicknames, {@code false} otherwise.
     *
     * @see Server#canManageNicknames(User)
     */
    public boolean canManageNicknames() {
        return server.canManageNicknames(user);
    }

    /**
     * Checks if the member can mute members on the server.
     *
     * @return {@code true} if the member can mute members, {@code false} otherwise.
     *
     * @see Server#canMuteMembers(User)
     */
    public boolean canMuteMembers() {
        return server.canMuteMembers(user);
    }

    /**
     * Checks if the member can deafen members on the server.
     *
     * @return {@code true} if the member can deafen members, {@code false} otherwise.
     *
     * @see Server#canDeafenMembers(User)
     */
    public boolean canDeafenMembers() {
        return server.canDeafenMembers(user);
    }

    /**
     * Checks if the member can move members on the server.
     *
     * @return {@code true} if the member can move members, {@code false} otherwise.
     *
     * @see Server#canMoveMembers(User)
     */
    public boolean canMoveMembers() {
        return server.canMoveMembers(user);
    }

    /**
     * Checks if the member can manage emojis on the server.
     *
     * @return {@code true} if the member can manage emojis, {@code false} otherwise.
     *
     * @see Server#canManageEmojis(User)
     */
    public boolean canManageEmojis() {
        return server.canManageEmojis(user);
    }

    /**
     * Checks if the member can manage roles on the server.
     *
     * @return {@code true} if the member can manage roles, {@code false} otherwise.
     */
    public boolean canManageRoles() {
        return server.canManageRoles(user);
    }

    /**
     * Checks if the member can manage the roles of the target member.
     *
     * @param target the user whose roles are to be managed.
     *
     * @return {@code true} if the member can manage the target's roles, {@code false} otherwise.
     *
     * @see Server#canManageRolesOf(User, User)
     */
    public boolean canManageRolesOf(User target) {
        return server.canManageRolesOf(user, target);
    }

    /**
     * Checks if the member can manage the target role.
     *
     * @param target the role that is to be managed.
     *
     * @return {@code true} if the member can manage the target role, {@code false} otherwise.
     *
     * @see Server#canManageRole(User, Role)
     */
    public boolean canManageRole(Role target) {
        return server.canManageRole(user, target);
    }

    /**
     * Checks if the member can manage the server.
     *
     * @return {@code true} if the member can manage the server, {@code false} otherwise.
     *
     * @see Server#canManage(User)
     */
    public boolean canManage() {
        return server.canManage(user);
    }

    /**
     * Checks if the member can kick users from the server.
     *
     * @return {@code true} if the member can kick users, {@code false} otherwise.
     *
     * @see Server#canKickUsers(User)
     */
    public boolean canKickUsers() {
        return server.canKickUsers(user);
    }

    /**
     * Checks if the member can kick the other user.
     *
     * @param userToKick the user which should be kicked.
     *
     * @return {@code true} if the member can kick the other user, {@code false} otherwise.
     *
     * @see Server#canKickUser(User, User)
     */
    public boolean canKickUser(User userToKick) {
        return server.canKickUser(user, userToKick);
    }

    /**
     * Checks if the member can ban users from the server.
     *
     * @return {@code true} if the member can ban users, {@code false} otherwise.
     *
     * @see Server#canBanUsers(User)
     */
    public boolean canBanUsers() {
        return server.canBanUsers(user);
    }

    /**
     * Checks if the member can ban the other user.
     *
     * @param userToBan the user which should be banned.
     *
     * @return {@code true} if the member can ban the other user, {@code false} otherwise.
     *
     * @see Server#canBanUser(User, User)
     */
    public boolean canBanUser(User userToBan) {
        return server.canBanUser(user, userToBan);
    }

    /* User implemented methods */

    @Override
    public String getDiscriminatedName() {
        return user.getDiscriminatedName();
    }

    @Override
    public String getDiscriminator() {
        return user.getDiscriminator();
    }

    @Override
    public boolean isBot() {
        return user.isBot();
    }

    @Override
    public Set<Activity> getActivities() {
        return user.getActivities();
    }

    @Override
    public UserStatus getStatus() {
        return user.getStatus();
    }

    @Override
    public UserStatus getStatusOnClient(DiscordClient discordClient) {
        return user.getStatusOnClient(discordClient);
    }

    @Override
    public EnumSet<UserFlag> getUserFlags() {
        return user.getUserFlags();
    }

    @Override
    public Optional<String> getAvatarHash() {
        return user.getAvatarHash();
    }

    @Override
    public Icon getAvatar() {
        return user.getAvatar();
    }

    @Override
    public Icon getAvatar(int i) {
        return user.getAvatar(i);
    }

    @Override
    public Optional<String> getServerAvatarHash(Server server) {
        return server.getUserServerAvatarHash(user);
    }

    @Override
    public Optional<Icon> getServerAvatar(Server server) {
        return user.getServerAvatar(server);
    }

    @Override
    public Optional<Icon> getServerAvatar(Server server, int i) {
        return user.getServerAvatar(server, i);
    }

    @Override
    public Icon getEffectiveAvatar(Server server) {
        return user.getEffectiveAvatar(server);
    }

    @Override
    public Icon getEffectiveAvatar(Server server, int i) {
        return user.getEffectiveAvatar(server, i);
    }

    @Override
    public boolean hasDefaultAvatar() {
        return user.hasDefaultAvatar();
    }

    @Override
    public Set<Server> getMutualServers() {
        return user.getMutualServers();
    }

    @Override
    public String getDisplayName(Server server) {
        return user.getDisplayName(server);
    }

    @Override
    public Optional<String> getNickname(Server server) {
        return server.getNickname(user);
    }

    @Override
    public Optional<Instant> getServerBoostingSinceTimestamp(Server server) {
        return server.getServerBoostingSinceTimestamp(user);
    }

    @Override
    public Optional<Instant> getTimeout(Server server) {
        return user.getTimeout(server);
    }

    @Override
    public boolean isPending(Server server) {
        return server.isPending(user.getId());
    }

    @Override
    public boolean isSelfMuted(Server server) {
        return server.isSelfMuted(user.getId());
    }

    @Override
    public boolean isSelfDeafened(Server server) {
        return server.isSelfDeafened(user.getId());
    }

    @Override
    public Optional<Instant> getJoinedAtTimestamp(Server server) {
        return server.getJoinedAtTimestamp(user);
    }

    @Override
    public List<Role> getRoles(Server server) {
        return server.getRoles(user);
    }

    @Override
    public Optional<Color> getRoleColor(Server server) {
        return server.getRoleColor(user);
    }

    @Override
    public Optional<PrivateChannel> getPrivateChannel() {
        return user.getPrivateChannel();
    }

    @Override
    public CompletableFuture<PrivateChannel> openPrivateChannel() {
        return user.openPrivateChannel();
    }

    @Override
    public DiscordApi getApi() {
        return user.getApi();
    }

    @Override
    public long getId() {
        return user.getId();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public ListenerManager<InteractionCreateListener> addInteractionCreateListener(InteractionCreateListener interactionCreateListener) {
        return user.addInteractionCreateListener(interactionCreateListener);
    }

    @Override
    public List<InteractionCreateListener> getInteractionCreateListeners() {
        return user.getInteractionCreateListeners();
    }

    @Override
    public ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(MessageComponentCreateListener messageComponentCreateListener) {
        return user.addMessageComponentCreateListener(messageComponentCreateListener);
    }

    @Override
    public List<MessageComponentCreateListener> getMessageComponentCreateListeners() {
        return user.getMessageComponentCreateListeners();
    }

    @Override
    public ListenerManager<ButtonClickListener> addButtonClickListener(ButtonClickListener buttonClickListener) {
        return user.addButtonClickListener(buttonClickListener);
    }

    @Override
    public List<ButtonClickListener> getButtonClickListeners() {
        return user.getButtonClickListeners();
    }

    @Override
    public ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(SelectMenuChooseListener selectMenuChooseListener) {
        return user.addSelectMenuChooseListener(selectMenuChooseListener);
    }

    @Override
    public List<SelectMenuChooseListener> getSelectMenuChooseListeners() {
        return user.getSelectMenuChooseListeners();
    }

    @Override
    public ListenerManager<ModalSubmitListener> addModalSubmitListener(ModalSubmitListener modalSubmitListener) {
        return null;
    }

    @Override
    public List<ModalSubmitListener> getModalSubmitListeners() {
        return null;
    }

    @Override
    public ListenerManager<AutocompleteCreateListener> addAutocompleteCreateListener(AutocompleteCreateListener autocompleteCreateListener) {
        return null;
    }

    @Override
    public List<AutocompleteCreateListener> getAutocompleteCreateListeners() {
        return null;
    }

    @Override
    public ListenerManager<UserContextMenuCommandListener> addUserContextMenuCommandListener(UserContextMenuCommandListener userContextMenuCommandListener) {
        return null;
    }

    @Override
    public List<UserContextMenuCommandListener> getUserContextMenuCommandListeners() {
        return null;
    }

    @Override
    public ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(MessageContextMenuCommandListener messageContextMenuCommandListener) {
        return null;
    }

    @Override
    public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners() {
        return null;
    }

    @Override
    public ListenerManager<SlashCommandCreateListener> addSlashCommandCreateListener(SlashCommandCreateListener slashCommandCreateListener) {
        return user.addSlashCommandCreateListener(slashCommandCreateListener);
    }

    @Override
    public List<SlashCommandCreateListener> getSlashCommandCreateListeners() {
        return user.getSlashCommandCreateListeners();
    }

    @Override
    public ListenerManager<UserChangeSelfMutedListener> addUserChangeSelfMutedListener(UserChangeSelfMutedListener userChangeSelfMutedListener) {
        return user.addUserChangeSelfMutedListener(userChangeSelfMutedListener);
    }

    @Override
    public List<UserChangeSelfMutedListener> getUserChangeSelfMutedListeners() {
        return user.getUserChangeSelfMutedListeners();
    }

    @Override
    public ListenerManager<UserChangeSelfDeafenedListener> addUserChangeSelfDeafenedListener(UserChangeSelfDeafenedListener userChangeSelfDeafenedListener) {
        return user.addUserChangeSelfDeafenedListener(userChangeSelfDeafenedListener);
    }

    @Override
    public List<UserChangeSelfDeafenedListener> getUserChangeSelfDeafenedListeners() {
        return user.getUserChangeSelfDeafenedListeners();
    }

    @Override
    public ListenerManager<UserChangeDiscriminatorListener> addUserChangeDiscriminatorListener(UserChangeDiscriminatorListener userChangeDiscriminatorListener) {
        return user.addUserChangeDiscriminatorListener(userChangeDiscriminatorListener);
    }

    @Override
    public List<UserChangeDiscriminatorListener> getUserChangeDiscriminatorListeners() {
        return user.getUserChangeDiscriminatorListeners();
    }

    @Override
    public ListenerManager<UserChangeServerAvatarListener> addUserChangeServerAvatarListener(UserChangeServerAvatarListener userChangeServerAvatarListener) {
        return null;
    }

    @Override
    public List<UserChangeServerAvatarListener> getUserChangeServerAvatarListeners() {
        return null;
    }

    @Override
    public ListenerManager<UserChangeActivityListener> addUserChangeActivityListener(UserChangeActivityListener userChangeActivityListener) {
        return user.addUserChangeActivityListener(userChangeActivityListener);
    }

    @Override
    public List<UserChangeActivityListener> getUserChangeActivityListeners() {
        return user.getUserChangeActivityListeners();
    }

    @Override
    public ListenerManager<UserChangeMutedListener> addUserChangeMutedListener(UserChangeMutedListener userChangeMutedListener) {
        return user.addUserChangeMutedListener(userChangeMutedListener);
    }

    @Override
    public List<UserChangeMutedListener> getUserChangeMutedListeners() {
        return user.getUserChangeMutedListeners();
    }

    @Override
    public ListenerManager<UserChangeDeafenedListener> addUserChangeDeafenedListener(UserChangeDeafenedListener userChangeDeafenedListener) {
        return user.addUserChangeDeafenedListener(userChangeDeafenedListener);
    }

    @Override
    public List<UserChangeDeafenedListener> getUserChangeDeafenedListeners() {
        return user.getUserChangeDeafenedListeners();
    }

    @Override
    public ListenerManager<UserChangePendingListener> addUserChangePendingListener(UserChangePendingListener userChangePendingListener) {
        return user.addUserChangePendingListener(userChangePendingListener);
    }

    @Override
    public List<UserChangePendingListener> getUserChangePendingListeners() {
        return user.getUserChangePendingListeners();
    }

    @Override
    public ListenerManager<UserChangeTimeoutListener> addUserChangeTimeoutListener(UserChangeTimeoutListener userChangeTimeoutListener) {
        return null;
    }

    @Override
    public List<UserChangeTimeoutListener> getUserChangeTimeoutListeners() {
        return null;
    }

    @Override
    public ListenerManager<UserStartTypingListener> addUserStartTypingListener(UserStartTypingListener userStartTypingListener) {
        return user.addUserStartTypingListener(userStartTypingListener);
    }

    @Override
    public List<UserStartTypingListener> getUserStartTypingListeners() {
        return user.getUserStartTypingListeners();
    }

    @Override
    public ListenerManager<UserChangeNicknameListener> addUserChangeNicknameListener(UserChangeNicknameListener userChangeNicknameListener) {
        return user.addUserChangeNicknameListener(userChangeNicknameListener);
    }

    @Override
    public List<UserChangeNicknameListener> getUserChangeNicknameListeners() {
        return user.getUserChangeNicknameListeners();
    }

    @Override
    public ListenerManager<UserChangeAvatarListener> addUserChangeAvatarListener(UserChangeAvatarListener userChangeAvatarListener) {
        return user.addUserChangeAvatarListener(userChangeAvatarListener);
    }

    @Override
    public List<UserChangeAvatarListener> getUserChangeAvatarListeners() {
        return user.getUserChangeAvatarListeners();
    }

    @Override
    public ListenerManager<UserChangeStatusListener> addUserChangeStatusListener(UserChangeStatusListener userChangeStatusListener) {
        return user.addUserChangeStatusListener(userChangeStatusListener);
    }

    @Override
    public List<UserChangeStatusListener> getUserChangeStatusListeners() {
        return user.getUserChangeStatusListeners();
    }

    @Override
    public ListenerManager<UserChangeNameListener> addUserChangeNameListener(UserChangeNameListener userChangeNameListener) {
        return user.addUserChangeNameListener(userChangeNameListener);
    }

    @Override
    public List<UserChangeNameListener> getUserChangeNameListeners() {
        return user.getUserChangeNameListeners();
    }

    @Override
    public ListenerManager<PrivateChannelCreateListener> addPrivateChannelCreateListener(PrivateChannelCreateListener privateChannelCreateListener) {
        return user.addPrivateChannelCreateListener(privateChannelCreateListener);
    }

    @Override
    public List<PrivateChannelCreateListener> getPrivateChannelCreateListeners() {
        return user.getPrivateChannelCreateListeners();
    }

    @Override
    public ListenerManager<PrivateChannelDeleteListener> addPrivateChannelDeleteListener(PrivateChannelDeleteListener privateChannelDeleteListener) {
        return user.addPrivateChannelDeleteListener(privateChannelDeleteListener);
    }

    @Override
    public List<PrivateChannelDeleteListener> getPrivateChannelDeleteListeners() {
        return user.getPrivateChannelDeleteListeners();
    }

    @Override
    public ListenerManager<ServerChannelChangeOverwrittenPermissionsListener> addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener serverChannelChangeOverwrittenPermissionsListener) {
        return user.addServerChannelChangeOverwrittenPermissionsListener(serverChannelChangeOverwrittenPermissionsListener);
    }

    @Override
    public List<ServerChannelChangeOverwrittenPermissionsListener> getServerChannelChangeOverwrittenPermissionsListeners() {
        return user.getServerChannelChangeOverwrittenPermissionsListeners();
    }

    @Override
    public ListenerManager<ServerVoiceChannelMemberLeaveListener> addServerVoiceChannelMemberLeaveListener(ServerVoiceChannelMemberLeaveListener serverVoiceChannelMemberLeaveListener) {
        return user.addServerVoiceChannelMemberLeaveListener(serverVoiceChannelMemberLeaveListener);
    }

    @Override
    public List<ServerVoiceChannelMemberLeaveListener> getServerVoiceChannelMemberLeaveListeners() {
        return user.getServerVoiceChannelMemberLeaveListeners();
    }

    @Override
    public ListenerManager<ServerVoiceChannelMemberJoinListener> addServerVoiceChannelMemberJoinListener(ServerVoiceChannelMemberJoinListener serverVoiceChannelMemberJoinListener) {
        return user.addServerVoiceChannelMemberJoinListener(serverVoiceChannelMemberJoinListener);
    }

    @Override
    public List<ServerVoiceChannelMemberJoinListener> getServerVoiceChannelMemberJoinListeners() {
        return user.getServerVoiceChannelMemberJoinListeners();
    }

    @Override
    public ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener reactionAddListener) {
        return user.addReactionAddListener(reactionAddListener);
    }

    @Override
    public List<ReactionAddListener> getReactionAddListeners() {
        return user.getReactionAddListeners();
    }

    @Override
    public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener reactionRemoveListener) {
        return user.addReactionRemoveListener(reactionRemoveListener);
    }

    @Override
    public List<ReactionRemoveListener> getReactionRemoveListeners() {
        return user.getReactionRemoveListeners();
    }

    @Override
    public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener messageCreateListener) {
        return user.addMessageCreateListener(messageCreateListener);
    }

    @Override
    public List<MessageCreateListener> getMessageCreateListeners() {
        return user.getMessageCreateListeners();
    }

    @Override
    public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener messageReplyListener) {
        return null;
    }

    @Override
    public List<MessageReplyListener> getMessageReplyListeners() {
        return null;
    }

    @Override
    public ListenerManager<UserRoleAddListener> addUserRoleAddListener(UserRoleAddListener userRoleAddListener) {
        return user.addUserRoleAddListener(userRoleAddListener);
    }

    @Override
    public List<UserRoleAddListener> getUserRoleAddListeners() {
        return user.getUserRoleAddListeners();
    }

    @Override
    public ListenerManager<UserRoleRemoveListener> addUserRoleRemoveListener(UserRoleRemoveListener userRoleRemoveListener) {
        return user.addUserRoleRemoveListener(userRoleRemoveListener);
    }

    @Override
    public List<UserRoleRemoveListener> getUserRoleRemoveListeners() {
        return user.getUserRoleRemoveListeners();
    }

    @Override
    public ListenerManager<ServerMemberLeaveListener> addServerMemberLeaveListener(ServerMemberLeaveListener serverMemberLeaveListener) {
        return user.addServerMemberLeaveListener(serverMemberLeaveListener);
    }

    @Override
    public List<ServerMemberLeaveListener> getServerMemberLeaveListeners() {
        return user.getServerMemberLeaveListeners();
    }

    @Override
    public ListenerManager<ServerMemberBanListener> addServerMemberBanListener(ServerMemberBanListener serverMemberBanListener) {
        return user.addServerMemberBanListener(serverMemberBanListener);
    }

    @Override
    public List<ServerMemberBanListener> getServerMemberBanListeners() {
        return user.getServerMemberBanListeners();
    }

    @Override
    public ListenerManager<ServerMemberUnbanListener> addServerMemberUnbanListener(ServerMemberUnbanListener serverMemberUnbanListener) {
        return user.addServerMemberUnbanListener(serverMemberUnbanListener);
    }

    @Override
    public List<ServerMemberUnbanListener> getServerMemberUnbanListeners() {
        return user.getServerMemberUnbanListeners();
    }

    @Override
    public ListenerManager<ServerMemberJoinListener> addServerMemberJoinListener(ServerMemberJoinListener serverMemberJoinListener) {
        return user.addServerMemberJoinListener(serverMemberJoinListener);
    }

    @Override
    public List<ServerMemberJoinListener> getServerMemberJoinListeners() {
        return user.getServerMemberJoinListeners();
    }

    @Override
    public <T extends UserAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addUserAttachableListener(T t) {
        return user.addUserAttachableListener(t);
    }

    @Override
    public <T extends UserAttachableListener & ObjectAttachableListener> void removeUserAttachableListener(T t) {
        user.removeUserAttachableListener(t);
    }

    @Override
    public <T extends UserAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getUserAttachableListeners() {
        return user.getUserAttachableListeners();
    }

    @Override
    public <T extends UserAttachableListener & ObjectAttachableListener> void removeListener(Class<T> aClass, T t) {
        user.removeListener(aClass, t);
    }
}
