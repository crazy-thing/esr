package net.crazything.esr.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Locale;

public class EnchantingSystemReImaginedConfigScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget safetyNetField;
    private TextFieldWidget maxBookshelvesField;

    private DoubleSliderWidget lapisSlider;
    private DoubleSliderWidget xpSlider;
    private DoubleSliderWidget lootChanceSlider;
    private DoubleSliderWidget curseFailureSlider;

    private static final int ROW1_Y = 35;
    private static final int ROW2_Y = 60;
    private static final int ROW3_Y = 95;
    private static final int ROW4_Y = 130;
    private static final int ROW5_Y = 165;
    private static final int ROW6_Y = 200;
    private static final int DONE_Y = 230;

    public EnchantingSystemReImaginedConfigScreen(Screen parent) {
        super(Text.literal("Enchanting System ReImagined Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        EnchantingSystemReImaginedConfig config = EnchantingSystemReImaginedConfig.get();

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Strip Loot Gear: " + (config.bestEffortLootStripOutsideLootTables ? "ON" : "OFF")),
                button -> {
                    config.bestEffortLootStripOutsideLootTables = !config.bestEffortLootStripOutsideLootTables;
                    button.setMessage(Text.literal(
                            "Strip Loot Gear: " + (config.bestEffortLootStripOutsideLootTables ? "ON" : "OFF")));
                }).dimensions(this.width / 2 - 160, ROW1_Y, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Allow Book Enchanting: " + (config.allowBookEnchanting ? "ON" : "OFF")),
                button -> {
                    config.allowBookEnchanting = !config.allowBookEnchanting;
                    button.setMessage(
                            Text.literal("Allow Book Enchanting: " + (config.allowBookEnchanting ? "ON" : "OFF")));
                }).dimensions(this.width / 2 + 10, ROW1_Y, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Book Chest Loot: " + (config.enchantedBookLootEnabled ? "ON" : "OFF")),
                button -> {
                    config.enchantedBookLootEnabled = !config.enchantedBookLootEnabled;
                    button.setMessage(
                            Text.literal("Book Chest Loot: " + (config.enchantedBookLootEnabled ? "ON" : "OFF")));
                }).dimensions(this.width / 2 - 160, ROW2_Y, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Consume Book: " + (config.consumeEnchantedBook ? "ON" : "OFF")),
                button -> {
                    config.consumeEnchantedBook = !config.consumeEnchantedBook;
                    button.setMessage(Text.literal("Consume Book: " + (config.consumeEnchantedBook ? "ON" : "OFF")));
                }).dimensions(this.width / 2 + 10, ROW2_Y, 150, 20).build());

        this.lootChanceSlider = new DoubleSliderWidget(
                this.width / 2 - 160, ROW3_Y, 150, 20,
                "Chance",
                config.enchantedBookLootChance,
                val -> config.enchantedBookLootChance = val);
        this.addDrawableChild(this.lootChanceSlider);

        this.maxBookshelvesField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 + 10, ROW3_Y, 150, 20,
                Text.literal("Max Bookshelves"));
        this.maxBookshelvesField.setText(String.valueOf(config.maxBookshelfDiscountCount));
        this.addDrawableChild(this.maxBookshelvesField);

        this.lapisSlider = new DoubleSliderWidget(
                this.width / 2 - 160, ROW4_Y, 150, 20,
                "Lapis",
                config.lapisDiscountPerBookshelf,
                val -> config.lapisDiscountPerBookshelf = val);
        this.addDrawableChild(this.lapisSlider);

        this.safetyNetField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 + 10, ROW4_Y, 150, 20,
                Text.literal("Safety Net Interval"));
        this.safetyNetField.setText(String.valueOf(config.safetyNetIntervalTicks));
        this.addDrawableChild(this.safetyNetField);

        this.xpSlider = new DoubleSliderWidget(
                this.width / 2 - 160, ROW5_Y, 150, 20,
                "XP",
                config.xpDiscountPerBookshelf,
                val -> config.xpDiscountPerBookshelf = val);
        this.addDrawableChild(this.xpSlider);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Edit Enchantment Costs..."),
                button -> {
                    this.saveAll(config);
                    this.client.setScreen(new EnchantingSystemReImaginedCostsScreen(this));
                }).dimensions(this.width / 2 + 10, ROW5_Y, 150, 20).build());

        this.curseFailureSlider = new DoubleSliderWidget(
                this.width / 2 - 160, ROW6_Y, 150, 20,
                "Curse Chance",
                config.curseFailureChance,
                val -> config.curseFailureChance = val);
        this.addDrawableChild(this.curseFailureSlider);

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.done"),
                button -> {
                    this.saveAll(config);
                    this.client.setScreen(this.parent);
                }).dimensions(this.width / 2 - 100, DONE_Y, 200, 20).build());
    }

    private void saveAll(EnchantingSystemReImaginedConfig config) {
        try {
            config.safetyNetIntervalTicks = Integer.parseInt(this.safetyNetField.getText());
        } catch (NumberFormatException ignored) {
        }
        try {
            config.maxBookshelfDiscountCount = Integer.parseInt(this.maxBookshelvesField.getText());
        } catch (NumberFormatException ignored) {
        }
        config.save();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Book Loot Chance (0–1):"), this.width / 2 - 160,
                ROW3_Y - 10, 0xA0A0A0);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Lapis Discount Per Shelf:"), this.width / 2 - 160,
                ROW4_Y - 10, 0xA0A0A0);
        context.drawTextWithShadow(this.textRenderer, Text.literal("XP Discount Per Shelf:"), this.width / 2 - 160,
                ROW5_Y - 10, 0xA0A0A0);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Curse Failure Chance (0–1):"), this.width / 2 - 160,
                ROW6_Y - 10, 0xA0A0A0);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Max Shelf Discount Count:"), this.width / 2 + 10,
                ROW3_Y - 10, 0xA0A0A0);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Safety Net Interval (Ticks):"), this.width / 2 + 10,
                ROW4_Y - 10, 0xA0A0A0);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    public static class DoubleSliderWidget extends SliderWidget {
        private final java.util.function.Consumer<Double> consumer;
        private final String prefix;

        public DoubleSliderWidget(int x, int y, int width, int height, String prefix, double value,
                java.util.function.Consumer<Double> consumer) {
            super(x, y, width, height,
                    Text.literal(prefix + ": " + String.format(Locale.ROOT, "%.2f", value)), value);
            this.prefix = prefix;
            this.consumer = consumer;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.literal(this.prefix + ": " + String.format(Locale.ROOT, "%.2f", this.value)));
        }

        @Override
        protected void applyValue() {
            this.consumer.accept(this.value);
        }

        public void setValueDirectly(double val) {
            this.value = Math.max(0.0, Math.min(1.0, val));
            this.updateMessage();
        }
    }
}
