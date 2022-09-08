/*
 * Copyright (c) 2021, Scott Foster <scott@sgfost.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sgf.multihighlight;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import net.runelite.client.callback.ClientThread;

@Slf4j
@PluginDescriptor(
	name = "Multicolor Highlights",
	description = "Set different color overlays for different NPCs",
	loadWhenOutdated = true
)

public class MulticolorHighlightsPlugin extends Plugin
{
	private static final String TAG = "Tag-";

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Client client;

	@Inject
	private MulticolorHighlightsConfig config;

	@Inject
	private MulticolorHighlightsOverlay overlay;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	/**
	 * NPCs in each highlight group
	 */
	private final Set<NPC> highlights1 = new HashSet<>();
	private final Set<NPC> highlights2 = new HashSet<>();
	private final Set<NPC> highlights3 = new HashSet<>();
	private final Set<NPC> highlights4 = new HashSet<>();
	private final Set<NPC> highlights5 = new HashSet<>();
	private final Set<NPC> highlights6 = new HashSet<>();
	private final Set<NPC> highlights7 = new HashSet<>();

	@Getter(AccessLevel.PACKAGE)
	private final List<Set<NPC>> groupHighlights = ImmutableList.of(
			highlights1, highlights2, highlights3, highlights4, highlights5, highlights6, highlights7
	);

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		clientThread.invoke(this::buildHighlights);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		clientThread.invoke(() -> {
			for (Set<NPC> highlights : groupHighlights) {
				highlights.clear();
			}
		});
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING) {
			for (Set<NPC> highlights : groupHighlights) {
				highlights.clear();		// prevent highlighting anything when not logged in
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if (!configChanged.getGroup().equals("multicolorhighlights")) return;
		clientThread.invoke(this::buildHighlights);
	}



	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned) {
		final NPC npc = npcSpawned.getNpc();
		final String npcName = npc.getName();

		if (npcName == null) return;

		int group = 1;
		for (Set<NPC> highlights : groupHighlights) {
			if (matchesNpcName(npcName, getHighlightNames(group))) {
				highlights.add(npc);
			}
			group++;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned) {
		final NPC npc = npcDespawned.getNpc();

		for (Set<NPC> highlights : groupHighlights) {
			highlights.remove(npc);
		}
	}

	@Provides
	MulticolorHighlightsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(MulticolorHighlightsConfig.class);
	}

	/**
	 * Add all the highlighted NPCs around to the collection for rendering
	 */
	private void buildHighlights() {
		int group = 1;

		for (Set<NPC> highlights : groupHighlights) {
			highlights.clear();
			if (client.getGameState() != GameState.LOGGED_IN && client.getGameState() != GameState.LOADING) {
				return;		// prevent highlighting anything when client open but not logged in
			}

			for (NPC npc : client.getNpcs()) {
				final String npcName = npc.getName();
				if (npcName == null) continue;
				if (matchesNpcName(npcName, getHighlightNames(group))) {
					highlights.add(npc);
				}
			}
			group++;
		}
	}

	private List<String> getHighlightNames(final int groupNum) {
		String npcCsv = "";
		switch (groupNum) {
			case 1: npcCsv = config.getNpcs1(); break;
			case 2: npcCsv = config.getNpcs2(); break;
			case 3: npcCsv = config.getNpcs3(); break;
			case 4: npcCsv = config.getNpcs4(); break;
			case 5: npcCsv = config.getNpcs5(); break;
			case 6: npcCsv = config.getNpcs6(); break;
			case 7: npcCsv = config.getNpcs7(); break;
		}
		return Text.fromCSV(npcCsv);
	}

	 protected Color getGroupColor(final int groupNum) {
		switch (groupNum) {
			case 1: return config.getGroup1Color();
			case 2: return config.getGroup2Color();
			case 3: return config.getGroup3Color();
			case 4: return config.getGroup4Color();
			case 5: return config.getGroup5Color();
			case 6: return config.getGroup6Color();
			case 7: return config.getGroup7Color();
		}
		return null;
	}

	protected Color getGroupFillColor(final int groupNum) {
		// use additional setting for fill opacity so there can be a visible outline
		int alpha;
		switch (groupNum) {
			case 1: alpha = config.getGroup1FillAlpha(); break;
			case 2: alpha = config.getGroup2FillAlpha(); break;
			case 3: alpha = config.getGroup3FillAlpha(); break;
			case 4: alpha = config.getGroup4FillAlpha(); break;
			case 5: alpha = config.getGroup5FillAlpha(); break;
			case 6: alpha = config.getGroup6FillAlpha(); break;
			case 7: alpha = config.getGroup7FillAlpha(); break;
			default: return null;
		}
		Color color = getGroupColor(groupNum);
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	private boolean matchesNpcName(String name, List<String> highlightNames) {
		for (String highlight : highlightNames) {
			if (WildcardMatcher.matches(highlight, name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a shift-right click menu option to add the NPC to one of the color groups
	 */
	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		final MenuEntry menuEntry = event.getMenuEntry();
		final MenuAction menuAction = menuEntry.getType();
		final NPC npc = menuEntry.getNpc();

		if (npc == null)
		{
			return;
		}

		if (menuAction == MenuAction.EXAMINE_NPC && client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			// Add tag options to Shift-Right click menu
			Color menuColor = getGroupColor(1);

			if (npc.getName() == null)
			{
				return;
			}

			client.createMenuEntry(-1)
					.setOption(ColorUtil.prependColorTag(TAG.concat("1"), menuColor))
					.setTarget(event.getTarget())
					.setIdentifier(event.getIdentifier())
					.setType(MenuAction.RUNELITE)
					.onClick(this::tag);
		}
	}

	private void tag(MenuEntry entry)
	{
		final int id = entry.getIdentifier();
		final NPC[] cachedNPCs = client.getCachedNPCs();
		final NPC npc = cachedNPCs[id];

		if (npc == null || npc.getName() == null)
		{
			return;
		}

		final String name = npc.getName();
		final List<String> listOfNpcs = new ArrayList<>(getHighlightNames(5));

		if (!listOfNpcs.removeIf(name::equalsIgnoreCase))
		{
			listOfNpcs.add(name);
		}

		config.setNpcToGroup5(Text.toCSV(listOfNpcs));
	}
}
