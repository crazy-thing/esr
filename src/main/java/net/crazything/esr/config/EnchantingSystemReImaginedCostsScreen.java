package net.crazything.esr.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EnchantingSystemReImaginedCostsScreen extends Screen {

    private final Screen parent;
    private CostsListWidget list;
    private TextFieldWidget searchField;

    public EnchantingSystemReImaginedCostsScreen(Screen parent) {
        super(Text.literal("Edit Enchantment Costs & Loot Weights"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.list = new CostsListWidget(this.client, this.width, this.height, 46, this.height - 32, 24);
        this.addSelectableChild(this.list);
        this.searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 22, 200, 18,
                Text.literal("Search..."));
        this.searchField.setChangedListener(text -> this.list.filter(text));
        this.addDrawableChild(this.searchField);
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.done"),
                button -> {
                    EnchantingSystemReImaginedConfig.get().save();
                    this.client.setScreen(this.parent);
                }).dimensions(this.width / 2 - 160, this.height - 26, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Sync Costs"),
                button -> {
                    EnchantingSystemReImaginedConfig config = EnchantingSystemReImaginedConfig.get();
                    EnchantingSystemReImaginedConfig.EnchantmentCost defaultCost = config.enchantmentCosts
                            .get("default");
                    if (defaultCost != null) {
                        for (Map.Entry<String, EnchantingSystemReImaginedConfig.EnchantmentCost> entry : config.enchantmentCosts
                                .entrySet()) {
                            if (!entry.getKey().equals("default") && !entry.getValue().isProtected) {
                                entry.getValue().lapis = defaultCost.lapis;
                                entry.getValue().xp = defaultCost.xp;
                            }
                        }
                        config.save();
                        this.list.filter(this.searchField.getText());
                    }
                }).dimensions(this.width / 2 - 55, this.height - 26, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Sync Weights"),
                button -> {
                    EnchantingSystemReImaginedConfig config = EnchantingSystemReImaginedConfig.get();
                    EnchantingSystemReImaginedConfig.EnchantmentCost defaultCost = config.enchantmentCosts
                            .get("default");
                    if (defaultCost != null) {
                        for (Map.Entry<String, EnchantingSystemReImaginedConfig.EnchantmentCost> entry : config.enchantmentCosts
                                .entrySet()) {
                            if (!entry.getKey().equals("default") && !entry.getValue().isProtected) {
                                entry.getValue().lootWeight = defaultCost.lootWeight;
                            }
                        }
                        config.save();
                        this.list.filter(this.searchField.getText());
                    }
                }).dimensions(this.width / 2 + 50, this.height - 26, 110, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.list.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 6, 0xFFFFFF);
        int headerY = 36;
        context.drawTextWithShadow(this.textRenderer, Text.literal("Lapis"), this.width / 2 - 60, headerY, 0x506DFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("XP"), this.width / 2 + 35, headerY, 0x80FF20);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Weight"), this.width / 2 + 130, headerY, 0xFFCC00);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    class CostsListWidget extends AlwaysSelectedEntryListWidget<CostsListWidget.Entry> {
        private final List<Entry> allEntries = new ArrayList<>();

        public CostsListWidget(net.minecraft.client.MinecraftClient client, int width, int height,
                int top, int bottom, int itemHeight) {
            super(client, width, height, top, bottom, itemHeight);

            EnchantingSystemReImaginedConfig config = EnchantingSystemReImaginedConfig.get();
            List<String> keys = new ArrayList<>(config.enchantmentCosts.keySet());
            keys.remove("default");
            Collections.sort(keys);
            keys.add(0, "default");

            for (String key : keys) {
                Entry entry = new Entry(key);
                this.allEntries.add(entry);
                this.addEntry(entry);
            }
        }

        public void filter(String query) {
            this.clearEntries();
            String lowerQuery = query.toLowerCase(Locale.ROOT).trim();
            for (Entry entry : this.allEntries) {
                if (lowerQuery.isEmpty() || entry.matches(lowerQuery)) {
                    this.addEntry(entry);
                }
            }
            this.setScrollAmount(0);
        }

        @Override
        public int getRowWidth() {
            return 420;
        }

        @Override
        protected int getScrollbarPositionX() {
            return this.width / 2 + 215;
        }

        class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
            private final String key;
            private final ButtonWidget toggleProtect;
            private final ButtonWidget decLapis;
            private final ButtonWidget incLapis;
            private final ButtonWidget decXp;
            private final ButtonWidget incXp;
            private final ButtonWidget decWeight;
            private final ButtonWidget incWeight;

            public Entry(String key) {
                this.key = key;
                EnchantingSystemReImaginedConfig.EnchantmentCost cost = EnchantingSystemReImaginedConfig
                        .get().enchantmentCosts.get(key);

                this.toggleProtect = ButtonWidget.builder(Text.literal(cost.isProtected ? "🔒" : "🔓"), btn -> {
                    cost.isProtected = !cost.isProtected;
                    btn.setMessage(Text.literal(cost.isProtected ? "🔒" : "🔓"));
                }).dimensions(0, 0, 20, 16).build();

                this.decLapis = ButtonWidget.builder(Text.literal("-"), btn -> {
                    if (cost.lapis > 0)
                        cost.lapis--;
                }).dimensions(0, 0, 15, 16).build();

                this.incLapis = ButtonWidget.builder(Text.literal("+"), btn -> {
                    cost.lapis++;
                }).dimensions(0, 0, 15, 16).build();

                this.decXp = ButtonWidget.builder(Text.literal("-"), btn -> {
                    if (cost.xp > 0)
                        cost.xp--;
                }).dimensions(0, 0, 15, 16).build();

                this.incXp = ButtonWidget.builder(Text.literal("+"), btn -> {
                    cost.xp++;
                }).dimensions(0, 0, 15, 16).build();

                this.decWeight = ButtonWidget.builder(Text.literal("-"), btn -> {
                    if (cost.lootWeight > 0)
                        cost.lootWeight--;
                }).dimensions(0, 0, 15, 16).build();

                this.incWeight = ButtonWidget.builder(Text.literal("+"), btn -> {
                    cost.lootWeight++;
                }).dimensions(0, 0, 15, 16).build();
            }

            public boolean matches(String query) {
                if (this.key.toLowerCase(Locale.ROOT).contains(query))
                    return true;
                if (!this.key.equals("default")) {
                    Enchantment enc = Registries.ENCHANTMENT.get(new Identifier(this.key));
                    if (enc != null) {
                        return Text.translatable(enc.getTranslationKey()).getString()
                                .toLowerCase(Locale.ROOT).contains(query);
                    }
                }
                return false;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                    int mouseX, int mouseY, boolean hovered, float delta) {
                String name = this.key;
                if (!this.key.equals("default")) {
                    Enchantment enc = Registries.ENCHANTMENT.get(new Identifier(this.key));
                    if (enc != null)
                        name = Text.translatable(enc.getTranslationKey()).getString();
                }

                EnchantingSystemReImaginedConfig.EnchantmentCost cost = EnchantingSystemReImaginedConfig
                        .get().enchantmentCosts.get(this.key);

                int weightX = x + entryWidth - 100;
                int xpX = x + entryWidth - 205;
                int lapisX = x + entryWidth - 310;
                int lockX = lapisX - 25;
                int nameMaxW = lockX - x - 8;
                String truncated = EnchantingSystemReImaginedCostsScreen.this.textRenderer.trimToWidth(name, nameMaxW);
                context.drawTextWithShadow(EnchantingSystemReImaginedCostsScreen.this.textRenderer, truncated, x + 5,
                        y + 4, 0xFFFFFF);

                this.toggleProtect.setX(lockX);
                this.toggleProtect.setY(y);
                this.decLapis.setX(lapisX);
                this.decLapis.setY(y);
                this.incLapis.setX(lapisX + 45);
                this.incLapis.setY(y);
                context.drawCenteredTextWithShadow(EnchantingSystemReImaginedCostsScreen.this.textRenderer,
                        Text.literal(cost.lapis + " L"), lapisX + 30, y + 4, 0x506DFF);
                this.decXp.setX(xpX);
                this.decXp.setY(y);
                this.incXp.setX(xpX + 45);
                this.incXp.setY(y);
                context.drawCenteredTextWithShadow(EnchantingSystemReImaginedCostsScreen.this.textRenderer,
                        Text.literal(cost.xp + " XP"), xpX + 30, y + 4, 0x80FF20);
                this.decWeight.setX(weightX);
                this.decWeight.setY(y);
                this.incWeight.setX(weightX + 45);
                this.incWeight.setY(y);
                int weightColor = (cost.lootWeight == 0) ? 0xFF4444 : 0xFFCC00;
                String weightLabel = (cost.lootWeight == 0) ? "OFF" : String.valueOf(cost.lootWeight);
                context.drawCenteredTextWithShadow(EnchantingSystemReImaginedCostsScreen.this.textRenderer,
                        Text.literal(weightLabel), weightX + 30, y + 4, weightColor);
                this.toggleProtect.render(context, mouseX, mouseY, delta);
                this.decLapis.render(context, mouseX, mouseY, delta);
                this.incLapis.render(context, mouseX, mouseY, delta);
                this.decXp.render(context, mouseX, mouseY, delta);
                this.incXp.render(context, mouseX, mouseY, delta);
                this.decWeight.render(context, mouseX, mouseY, delta);
                this.incWeight.render(context, mouseX, mouseY, delta);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (this.toggleProtect.mouseClicked(mouseX, mouseY, button))
                    return true;
                if (this.decLapis.mouseClicked(mouseX, mouseY, button))
                    return true;
                if (this.incLapis.mouseClicked(mouseX, mouseY, button))
                    return true;
                if (this.decXp.mouseClicked(mouseX, mouseY, button))
                    return true;
                if (this.incXp.mouseClicked(mouseX, mouseY, button))
                    return true;
                if (this.decWeight.mouseClicked(mouseX, mouseY, button))
                    return true;
                if (this.incWeight.mouseClicked(mouseX, mouseY, button))
                    return true;
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public Text getNarration() {
                return Text.literal(this.key);
            }
        }
    }
}
