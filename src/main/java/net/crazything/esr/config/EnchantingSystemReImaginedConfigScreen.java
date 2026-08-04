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
    private TextFieldWidget lapisDiscountField;
    private TextFieldWidget xpDiscountField;
    private TextFieldWidget lootChanceField;

    private DoubleSliderWidget lapisSlider;
    private DoubleSliderWidget xpSlider;
    private DoubleSliderWidget lootChanceSlider;
    private DoubleSliderWidget curseFailureSlider;
    private TextFieldWidget curseFailureField;

    private static final int ROW1_Y = 36;
    private static final int ROW2_Y = 62;
    private static final int ROW3_Y = 96;
    private static final int ROW4_Y = 126;
    private static final int ROW5_Y = 152;
    private static final int ROW6_Y = 182;
    private static final int DONE_Y = 212;

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
                }).dimensions(this.width / 2 - 160, ROW2_Y, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Consume Book: " + (config.consumeEnchantedBook ? "ON" : "OFF")),
                button -> {
                    config.consumeEnchantedBook = !config.consumeEnchantedBook;
                    button.setMessage(Text.literal("Consume Book: " + (config.consumeEnchantedBook ? "ON" : "OFF")));
                }).dimensions(this.width / 2 - 55, ROW2_Y, 110, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Edit Enchantment Costs..."),
                button -> {
                    this.saveAll(config);
                    this.client.setScreen(new EnchantingSystemReImaginedCostsScreen(this));
                }).dimensions(this.width / 2 + 60, ROW2_Y, 100, 20).build());

        this.lootChanceField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 55, ROW3_Y, 45, 20,
                Text.literal("Loot Chance Input"));
        this.lootChanceField.setText(String.format(Locale.ROOT, "%.2f", config.enchantedBookLootChance));
        this.addDrawableChild(this.lootChanceField);

        this.lootChanceSlider = new DoubleSliderWidget(
                this.width / 2 - 160, ROW3_Y, 100, 20,
                "Book Chance",
                config.enchantedBookLootChance,
                val -> {
                    config.enchantedBookLootChance = val;
                    this.lootChanceField.setText(String.format(Locale.ROOT, "%.2f", val));
                });
        this.addDrawableChild(this.lootChanceSlider);

        this.lootChanceField.setChangedListener(text -> {
            try {
                double val = Double.parseDouble(text);
                if (val >= 0.0 && val <= 1.0) {
                    config.enchantedBookLootChance = val;
                    this.lootChanceSlider.setValueDirectly(val);
                }
            } catch (NumberFormatException ignored) {
            }
        });

        this.maxBookshelvesField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 + 10, ROW3_Y, 150, 20,
                Text.literal("Max Bookshelves"));
        this.maxBookshelvesField.setText(String.valueOf(config.maxBookshelfDiscountCount));
        this.addDrawableChild(this.maxBookshelvesField);

        this.lapisDiscountField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 55, ROW4_Y, 45, 20,
                Text.literal("Lapis Discount Input"));
        this.lapisDiscountField.setText(String.format(Locale.ROOT, "%.2f", config.lapisDiscountPerBookshelf));
        this.addDrawableChild(this.lapisDiscountField);

        this.lapisSlider = new DoubleSliderWidget(
                this.width / 2 - 160, ROW4_Y, 100, 20,
                "Lapis",
                config.lapisDiscountPerBookshelf,
                val -> {
                    config.lapisDiscountPerBookshelf = val;
                    this.lapisDiscountField.setText(String.format(Locale.ROOT, "%.2f", val));
                });
        this.addDrawableChild(this.lapisSlider);

        this.lapisDiscountField.setChangedListener(text -> {
            try {
                double val = Double.parseDouble(text);
                if (val >= 0.0 && val <= 1.0) {
                    config.lapisDiscountPerBookshelf = val;
                    this.lapisSlider.setValueDirectly(val);
                }
            } catch (NumberFormatException ignored) {
            }
        });

        this.xpDiscountField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 55, ROW5_Y, 45, 20,
                Text.literal("XP Discount Input"));
        this.xpDiscountField.setText(String.format(Locale.ROOT, "%.2f", config.xpDiscountPerBookshelf));
        this.addDrawableChild(this.xpDiscountField);

        this.xpSlider = new DoubleSliderWidget(
                this.width / 2 - 160, ROW5_Y, 100, 20,
                "XP",
                config.xpDiscountPerBookshelf,
                val -> {
                    config.xpDiscountPerBookshelf = val;
                    this.xpDiscountField.setText(String.format(Locale.ROOT, "%.2f", val));
                });
        this.addDrawableChild(this.xpSlider);

        this.xpDiscountField.setChangedListener(text -> {
            try {
                double val = Double.parseDouble(text);
                if (val >= 0.0 && val <= 1.0) {
                    config.xpDiscountPerBookshelf = val;
                    this.xpSlider.setValueDirectly(val);
                }
            } catch (NumberFormatException ignored) {
            }
        });

        this.safetyNetField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 + 10, ROW5_Y, 150, 20,
                Text.literal("Safety Net Interval"));
        this.safetyNetField.setText(String.valueOf(config.safetyNetIntervalTicks));
        this.addDrawableChild(this.safetyNetField);

        this.curseFailureField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 55, ROW6_Y, 45, 20,
                Text.literal("Curse Failure Input"));
        this.curseFailureField.setText(String.format(Locale.ROOT, "%.2f", config.curseFailureChance));
        this.addDrawableChild(this.curseFailureField);

        this.curseFailureSlider = new DoubleSliderWidget(
                this.width / 2 - 160, ROW6_Y, 100, 20,
                "Curse Chance",
                config.curseFailureChance,
                val -> {
                    config.curseFailureChance = val;
                    this.curseFailureField.setText(String.format(Locale.ROOT, "%.2f", val));
                });
        this.addDrawableChild(this.curseFailureSlider);

        this.curseFailureField.setChangedListener(text -> {
            try {
                double val = Double.parseDouble(text);
                if (val >= 0.0 && val <= 1.0) {
                    config.curseFailureChance = val;
                    this.curseFailureSlider.setValueDirectly(val);
                }
            } catch (NumberFormatException ignored) {
            }
        });

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
        try {
            double val = Double.parseDouble(this.lapisDiscountField.getText());
            if (val >= 0.0 && val <= 1.0)
                config.lapisDiscountPerBookshelf = val;
        } catch (NumberFormatException ignored) {
        }
        try {
            double val = Double.parseDouble(this.xpDiscountField.getText());
            if (val >= 0.0 && val <= 1.0)
                config.xpDiscountPerBookshelf = val;
        } catch (NumberFormatException ignored) {
        }
        try {
            double val = Double.parseDouble(this.lootChanceField.getText());
            if (val >= 0.0 && val <= 1.0)
                config.enchantedBookLootChance = val;
        } catch (NumberFormatException ignored) {
        }
        try {
            double val = Double.parseDouble(this.curseFailureField.getText());
            if (val >= 0.0 && val <= 1.0)
                config.curseFailureChance = val;
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
                ROW5_Y - 10, 0xA0A0A0);

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
