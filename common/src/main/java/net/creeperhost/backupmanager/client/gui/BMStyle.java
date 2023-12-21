package net.creeperhost.backupmanager.client.gui;

import net.creeperhost.polylib.client.PolyPalette;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.Assembly;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Axis;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BMStyle {

    public static class Flat {

        public static GuiElement<?> background(GuiParent<?> parent) {
            return PolyPalette.Flat.background(parent);
        }

        public static GuiElement<?> contentArea(GuiElement<?> parent) {
            return PolyPalette.Flat.contentArea(parent);
        }

        public static GuiButton button(GuiElement<?> parent, Component label) {
            return PolyPalette.Flat.button(parent, label);
        }

        public static GuiButton button(GuiElement<?> parent, @Nullable Supplier<Component> label) {
            return PolyPalette.Flat.button(parent, label);
        }

        public static GuiButton buttonCaution(GuiElement<?> parent, Component label) {
            return PolyPalette.Flat.buttonCaution(parent, label);
        }

        public static GuiButton buttonCaution(GuiElement<?> parent, @Nullable Supplier<Component> label) {
            return PolyPalette.Flat.buttonCaution(parent, label);
        }

        public static GuiButton buttonPrimary(GuiElement<?> parent, Component label) {
            return PolyPalette.Flat.buttonPrimary(parent, label);
        }

        public static GuiButton buttonPrimary(GuiElement<?> parent, @Nullable Supplier<Component> label) {
            return PolyPalette.Flat.buttonPrimary(parent, label);
        }

        public static Assembly<? extends GuiElement<?>, GuiSlider> scrollBar(GuiElement<?> parent, Axis axis) {
            return PolyPalette.Flat.scrollBar(parent, axis);
        }

        //BM Specific

        public static int listEntryBorder(boolean hoveredOrSelected) {
            return hoveredOrSelected ? 0xFFFFFFFF : 0x40FFFFFF;
        }

        public static int listEntryBackground(boolean hoveredOrSelected) {
            return hoveredOrSelected ? 0x40FFFFFF : 0;
        }
    }
}