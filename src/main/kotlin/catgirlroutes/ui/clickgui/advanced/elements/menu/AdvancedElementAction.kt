package catgirlroutes.ui.clickgui.advanced.elements.menu

import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.ui.clickgui.advanced.AdvancedMenu
import catgirlroutes.ui.clickgui.advanced.elements.AdvancedElement
import catgirlroutes.ui.clickgui.advanced.elements.AdvancedElementType
import catgirlroutes.ui.clickgui.util.FontUtil

/**
 * Provides the Button for action settings in the advanced gui.
 *
 * @author Aton
 */
class AdvancedElementAction(parent: AdvancedMenu, module: Module, setting: ActionSetting) :
    AdvancedElement<ActionSetting>(parent, module, setting, AdvancedElementType.ACTION) {

    /**
     * Render the element
     */
    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float) : Int{
        FontUtil.drawString(setting.name,  1,  2, -0x1)
        return this.settingHeight
    }

    /**
     * Handles mouse clicks for this element and returns true if an action was performed.
     * Used to activate the elements action.
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isButtonHovered(mouseX, mouseY)) {
            setting.doAction()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Checks whether this element is hovered
     */
    private fun isButtonHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y  && mouseY <= parent.y + y + settingHeight
    }
}