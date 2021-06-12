/*
    Copyright(c) 2021 AuroraLS3

    The MIT License(MIT)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files(the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions :
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/
package com.djrapitops.extension;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.annotation.DataBuilderProvider;
import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;
import com.plotsquared.core.api.PlotAPI;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * DataExtension.
 *
 * @author AuroraLS3
 */
@PluginInfo(name = "PlotSquared", iconName = "square", iconFamily = Family.SOLID, color = Color.GREEN)
public class PlotSquaredExtension implements DataExtension {

    private final PlotAPI plotAPI;

    public PlotSquaredExtension() {
        plotAPI = new PlotAPI();
    }

    @Override
    public CallEvents[] callExtensionMethodsOn() {
        return CallEvents.values();
    }

    @NumberProvider(
            text = "Plots",
            iconName = "square", iconFamily = Family.REGULAR, iconColor = Color.GREEN
    )
    public long plotCount() {
        return plotAPI.getAllPlots().size();
    }

    @DataBuilderProvider
    public ExtensionDataBuilder playerData(UUID playerUUID) {
        PlotPlayer<?> player = Optional.ofNullable(plotAPI.wrapPlayer(playerUUID))
                .orElseThrow(NotReadyException::new);

        int allowedPlots = player.getAllowedPlots();
        int plotCount = player.getPlotCount();

        Table.Factory plots = Table.builder()
                .columnOne("Id", Icon.called("square").of(Family.REGULAR).build())
                .columnTwo("World", Icon.called("map").build())
                .columnThree("Center", Icon.called("map-marker-alt").build());

        for (Plot plot : plotAPI.getPlayerPlots(player)) {
            PlotId plotID = plot.getId();
            Location center = getCenter(plot.getAllCorners());
            plots.addRow(plotID.toDashSeparatedString(), center.getWorld(), "x:" + center.getX() + " z:" + center.getZ());
        }

        return newExtensionDataBuilder()
                .addValue(String.class, valueBuilder("Plots")
                        .description("How many of the allowed plots the player has claimed")
                        .icon(Icon.called("square").of(Family.REGULAR).of(Color.GREEN).build())
                        .showInPlayerTable()
                        .buildString(plotCount + " / " + allowedPlots))
                .addTable("plots", plots.build(), Color.GREEN);
    }

    public Location getCenter(Collection<Location> corners) {
        String world = "-";
        int xSum = 0;
        int zSum = 0;
        int count = 0;
        for (Location corner : corners) {
            world = corner.getWorld();
            xSum += corner.getX();
            zSum += corner.getZ();
            count++;
        }
        return new Location(world, xSum / count, 0, zSum / count);
    }
}