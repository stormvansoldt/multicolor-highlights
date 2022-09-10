/*
 * Copyright (c) 2022, Storm, <https://github.com/stormvansoldt>
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.screenmarkers.ScreenMarkerOverlay;
import net.runelite.client.plugins.screenmarkers.ScreenMarkerPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;

@Slf4j
public class MulticolorHighlightsPanel extends PluginPanel
{
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;

	// Display this panel if there is no saved color configurations
	private final PluginErrorPanel noColorsPanel = new PluginErrorPanel();

	// Title panel data
	private final JPanel titlePanel;
	private final JLabel addGroup = new JLabel(ADD_ICON);
	private final JLabel title = new JLabel();
	private final JPanel groupsView = new JPanel(new GridBagLayout());

	private final MulticolorHighlightsPlugin plugin;
	private final MulticolorHighlightsConfig config;

	static
	{
		final BufferedImage addIcon = ImageUtil.loadImageResource(MulticolorHighlightsPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));
	}

	public MulticolorHighlightsPanel(MulticolorHighlightsPlugin plugin, MulticolorHighlightsConfig config)
	{
		this.plugin = plugin;
		this.config = config;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Create layout panel for wrapping
		final JPanel layoutPanel = new JPanel();
		layoutPanel.setBackground(ColorScheme.GRAND_EXCHANGE_LIMIT);
		layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
		add(layoutPanel, BorderLayout.NORTH);

		// Top title panel with plugin name and "Add new group" icon
		titlePanel = buildTitlePanel();

		layoutPanel.add(titlePanel);

		// Add default panel if no color groups exist
		noColorsPanel.setContent("Multicolor Highlights", "Tag and highlight NPCs in different colors.");
		//add(noColorsPanel);
	}

	/**
	 * Creates the panel which holds the name of the plugin and the "add new" color group.
	 * This panel sits at the very top of the overall panel view.
	 */
	private JPanel buildTitlePanel()
	{
		final JPanel titleContainer = new JPanel();
		titleContainer.setLayout(new BorderLayout());
		titleContainer.setBorder(new EmptyBorder(1, 0, 10, 0));
		titleContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		titleContainer.add(title, BorderLayout.WEST);
		titleContainer.add(addGroup, BorderLayout.EAST);

		title.setText("Multi Color NPC Highlights");
		title.setForeground(Color.WHITE);

		addGroup.setToolTipText("Add new color group");
		addGroup.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				log.debug("Add button was clicked!");
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				addGroup.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				addGroup.setIcon(ADD_ICON);
			}
		});

		return titleContainer;
	}
}
