/***************************************************************************
 * Project file:    NPlugins - NCuboid - GeneralRegion.java                *
 * Full Class name: fr.ribesg.bukkit.ncuboid.beans.GeneralRegion           *
 *                                                                         *
 *                Copyright (c) 2012-2014 Ribesg - www.ribesg.fr           *
 *   This file is under GPLv3 -> http://www.gnu.org/licenses/gpl-3.0.txt   *
 *    Please contact me at ribesg[at]yahoo.fr if you improve this file!    *
 ***************************************************************************/

package fr.ribesg.bukkit.ncuboid.beans;

import fr.ribesg.bukkit.ncore.common.NLocation;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import java.util.Set;

public abstract class GeneralRegion extends Region {

	public static enum RegionType {
		// Cuboid Region
		CUBOID,

		// World Region
		WORLD
	}

	// Identification / informations related
	private String     worldName;
	private RegionType type;

	// Protection related
	private final Rights rights;
	private       int    priority;

	// Flags related
	private final Flags      flags;
	private final Attributes attributes;

	// This is for Dynmap!
	private final boolean dynmapable;

	// Create a new Region, when user select points etc
	public GeneralRegion(final String worldName, final RegionType type, final int priority) {
		this(worldName, type, new Rights(), priority, new Flags(), new Attributes());
	}

	public GeneralRegion(final String worldName, final RegionType type, final Rights rights, final int priority, final Flags flags, final Attributes attributes) {
		setWorldName(worldName);
		setType(type);
		switch (type) {
			case CUBOID:
				this.dynmapable = true;
				break;
			case WORLD:
			default:
				this.dynmapable = false;
				break;
		}
		this.rights = rights;
		setPriority(priority);
		this.flags = flags;
		this.attributes = attributes;
	}

	// Location check
	public boolean contains(final Location loc) {
		return contains(new NLocation(loc));
	}

	public abstract boolean contains(final NLocation loc);

	public abstract String getRegionName();

	public abstract long getTotalSize();

	public Attributes getAttributes() {
		return attributes;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(final int priority) {
		this.priority = priority;
	}

	public RegionType getType() {
		return type;
	}

	public void setType(final RegionType type) {
		this.type = type;
	}

	public String getWorldName() {
		return worldName;
	}

	public void setWorldName(final String worldName) {
		this.worldName = worldName;
	}

	public boolean isDynmapable() {
		return this.dynmapable;
	}

	public boolean getFlag(final Flag f) {
		return flags.getFlag(f);
	}

	public void setFlag(final Flag f, final boolean b) {
		flags.setFlag(f, b);
	}

	public Integer getIntAttribute(final Attribute att) {
		return attributes.getIntAttribute(att);
	}

	public void setIntAttribute(final Attribute att, final Integer i) {
		attributes.setIntAttribute(att, i);
	}

	public Location getLocAttribute(final Attribute att) {
		return attributes.getLocAttribute(att);
	}

	public void setLocAttribute(final Attribute att, final Location loc) {
		attributes.setLocAttribute(att, loc);
	}

	public Vector getVectAttribute(final Attribute att) {
		return attributes.getVectAttribute(att);
	}

	public void setVectAttribute(final Attribute att, final Vector v) {
		attributes.setVectAttribute(att, v);
	}

	public String getStringRepresentation(final Attribute att) {
		return attributes.getStringRepresentation(att);
	}

	public boolean isUser(final CommandSender sender) {
		return rights.isUser(sender);
	}

	public boolean isUserName(final String name) {
		return rights.isUserName(name);
	}

	public boolean isAdmin(final CommandSender sender) {
		return rights.isAdmin(sender);
	}

	public boolean isAdminName(final String name) {
		return rights.isAdminName(name);
	}

	public boolean isAllowedGroup(final String groupName) {
		return rights.isAllowedGroup(groupName);
	}

	public boolean isAllowedCommand(final String command) {
		return rights.isAllowedCommand(command);
	}

	public Set<String> getUsers() {
		return rights.getUsers();
	}

	public Set<String> getAdmins() {
		return rights.getAdmins();
	}

	public Set<String> getAllowedGroups() {
		return rights.getAllowedGroups();
	}

	public Set<String> getDisallowedCommands() {
		return rights.getDisallowedCommands();
	}

	public void removeUser(final String playerName) {
		rights.removeUser(playerName);
	}

	public void addUser(final String playerName) {
		rights.addUser(playerName);
	}

	public void addAdmin(final String playerName) {
		rights.addAdmin(playerName);
	}

	public void removeAdmin(final String playerName) {
		rights.removeAdmin(playerName);
	}

	public void allowGroup(final String groupName) {
		rights.allowGroup(groupName);
	}

	public void denyGroup(final String groupName) {
		rights.denyGroup(groupName);
	}

	public void allowCommand(final String command) {
		rights.allowCommand(command);
	}

	public void denyCommand(final String command) {
		rights.denyCommand(command);
	}
}
