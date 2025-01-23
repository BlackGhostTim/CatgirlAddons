package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.config.InventoryButtonsConfig
import catgirlroutes.config.InventoryButtonsConfig.allButtons
import catgirlroutes.mixins.accessors.AccessorGuiContainer
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.ui.misc.inventorybuttons.InventoryButtonEditor
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.render.HUDRenderUtils
import com.google.common.collect.Lists
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.client.config.GuiUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse

object InventoryButtons : Module(
    "Inventory buttons",
    Category.MISC,
    tag = TagType.WHIP
) { // todo: Eq overlay
    val equipmentOverlay: BooleanSetting = BooleanSetting("Equipment Overlay", false)

    val editMode: ActionSetting = ActionSetting("Edit") { display = InventoryButtonEditor() }

    init {
        addSettings(this.equipmentOverlay, this.editMode)
    }

    @SubscribeEvent
    fun onDrawScreenPost(event: GuiScreenEvent.DrawScreenEvent.Post) {
        if (mc.currentScreen !is GuiInventory) return
        val accessor = event.gui as AccessorGuiContainer

        allButtons.filter { it.isActive && (it.isEquipment == this.equipmentOverlay.enabled) }
            .forEach { button ->
                GlStateManager.pushMatrix()
                GlStateManager.disableLighting()

                button.render(accessor.guiLeft.toDouble(), accessor.guiTop.toDouble())

                GlStateManager.enableLighting()
                GlStateManager.popMatrix()
            }

        // different loop so hovering text is always above the buttons
        allButtons.filter { it.isActive && (it.isEquipment == this.equipmentOverlay.enabled) }
            .forEach { button ->
                if (button.isHovered(event.mouseX - accessor.guiLeft, event.mouseY - accessor.guiTop)) {
                    GuiUtils.drawHoveringText(
                        Lists.newArrayList("§7/${button.command.replace("/", "")}"),
                        event.mouseX, event.mouseY,
                        event.gui.width, event.gui.height,
                        -1, mc.fontRendererObj
                    )
                }
            }
    }

    @SubscribeEvent
    fun onMouseInputPre(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (mc.currentScreen !is GuiInventory) return

        val sr = ScaledResolution(mc)

        val mouseX: Int = Mouse.getX() * sr.scaledWidth / mc.displayWidth
        val mouseY: Int = sr.scaledHeight - (Mouse.getY() * sr.scaledHeight / mc.displayHeight)

        val accessor = event.gui as AccessorGuiContainer

        allButtons.filter { it.isActive && it.isHovered(mouseX - accessor.guiLeft, mouseY - accessor.guiTop) && Mouse.getEventButtonState() }.forEach { it.action }
    }
}