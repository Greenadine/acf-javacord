/*
 * Copyright (c) 2021 Kevin Zuman (Greenadine)
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

import com.google.common.base.Preconditions;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link User} that is part of a specific {@link Server}. Contains all server-specific information about a {@code User}.
 */
public class Member {

    private final User user;
    private final Server server;

    public Member(@NotNull User user, @NotNull Server server) {
        Preconditions.checkArgument(server.isMember(user), "User is not a member of the server");

        this.user = user;
        this.server = server;
    }

    /**
     * Gets the {@link User} instance of the member.
     *
     * @return the member's {@code User} instance.
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns whether the member is a bot.
     *
     * @return {@code true} if the member is a bot, {@code false} otherwise.
     *
     * @see User#isBot()
     */
    public boolean isBot() {
        return user.isBot();
    }

    /**
     * Gets the {@link Server} of the member.
     *
     * @return the member's {@code Server}.
     */
    public Server getServer() {
        return server;
    }

    /**
     * Gets the ID of the member.
     *
     * @return the ID of the member.
     *
     * @see User#getId()
     */
    public long getId() {
        return user.getId();
    }

    /**
     * Gets the display name of the member.
     *
     * <p>Gets the nickname of the member if present, otherwise the member's username.</p>
     *
     * @return the member's display name.
     *
     * @see #getNickname()
     * @see #getName()
     */
    public String getDisplayName() {
        return getNickname().orElse(getName());
    }

    /**
     * Gets the name of the member.
     *
     * @return the member's name.
     *
     * @see User#getName()
     */
    public String getName() {
        return user.getName();
    }

    /**
     * Gets the discriminated name of the member.
     *
     * @return the member's discriminated name.
     *
     * @see User#getDiscriminatedName()
     */
    public String getDiscriminatedName() {
        return user.getDiscriminatedName();
    }

    /**
     * Gets the discriminator of the member.
     *
     * @return the member's discriminator.
     *
     * @see User#getDiscriminator()
     */
    public String getDiscriminator() {
        return user.getDiscriminator();
    }

    /**
     * Gets the timestamp of when the member joined the server.
     *
     * @return the {@link Instant} timestamp of when the member joined the server.
     *
     * @see Server#getJoinedAtTimestamp(User)
     */
    public Optional<Instant> getJoinedAtTimestamp() {
        return server.getJoinedAtTimestamp(user);
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
     * @return a future to check if the update was successful.
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
     * @return a future to check if the update was successful.
     *
     * @see Server#updateNickname(User, String)
     */
    public CompletableFuture<Void> updateNickname(String nickname, String reason) {
        return server.updateNickname(user, nickname, reason);
    }

    /**
     * Removes the nickname of the member.
     *
     * @return a future to check if the update was successful.
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
     * @return a future to check if the update was successful.
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
     * @return a future to check if the update was successful.
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
     * @return a future to check if the update was successful.
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
     * @return a future to check if the update was successful.
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
     * @return a future to check if the update was successful.
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
     * @return a future to check if the update was successful.
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
     * @return a future to check if the update was successful.
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
     * Moves the member to the given channel on the server.
     *
     * @param channel the channel to move the member to.
     *
     * @return a future to check if the move was successful.
     *
     * @see Server#moveUser(User, ServerVoiceChannel)
     */
    public CompletableFuture<Void> moveToVoiceChannel(ServerVoiceChannel channel) {
        return server.moveUser(user, channel);
    }

    /**
     * Kicks the member from any voice channel.
     *
     * @return a future to check if the kick was successful.
     *
     * @see Server#kickUserFromVoiceChannel(User)
     */
    public CompletableFuture<Void> kickFromVoiceChannel() {
        return server.kickUserFromVoiceChannel(user);
    }

    /**
     * Mutes the member on the server.
     *
     * @return a future to check if the mute was successful.
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
     * @return a future to check if the mute was successful.
     *
     * @see Server#muteUser(User, String)
     */
    public CompletableFuture<Void> mute(String reason) {
        return server.muteUser(user, reason);
    }

    /**
     * Unmutes the member on the server.
     *
     * @return a future to check if the unmute was successful.
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
     * @return a future to check if the unmute was successful.
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
     * @return a future to check if the deafen was successful.
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
     * @return a future to check if the deafen was successful.
     *
     * @see Server#deafenUser(User, String)
     */
    public CompletableFuture<Void> deafen(String reason) {
        return server.deafenUser(user, reason);
    }

    /**
     * Undeafens the member on the server.
     *
     * @return a future to check if the undeafen was successful.
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
     * @return a future to check if the undeafen was successful.
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
     * Kicks the member from the server.
     *
     * @return a future to check if the kick was successful.
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
     * @return a future to check if the kick was successful.
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
     * @param deleteMessageDays the number of days to delete the messages for (0-7).
     *
     * @return a future to check if the ban was successful.
     *
     * @see Server#banUser(User, int)
     */
    public CompletableFuture<Void> ban(int deleteMessageDays) {
        return server.banUser(user, deleteMessageDays);
    }

    /**
     * Bans the member from the server.
     *
     * @param deleteMessageDays the number of days to delete the messages for (0-7).
     * @param reason the audit log reason for this action.
     *
     * @return a future to check if the ban was successful.
     *
     * @see Server#banUser(User, int, String)
     */
    public CompletableFuture<Void> ban(int deleteMessageDays, String reason) {
        return server.banUser(user, deleteMessageDays, reason);
    }

    /**
     * Unbans the member from the server.
     *
     * @return a future to check if the unban was successful.
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
     * @return a future to check if the unban was successful.
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
     * @return {@code true} if the member has all of the given permissions, {@code false} otherwise.
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
     * Gets the highest role of the member in the server.
     *
     * @return the highest role of the member.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public Role getHighestRole() {
        return server.getHighestRole(user).get();
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
}
