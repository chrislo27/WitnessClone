package io.github.chrislo27.witnessclone.init

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.TextureLoader
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import io.github.chrislo27.toolboks.registry.AssetRegistry


class DefaultAssetLoader : AssetRegistry.IAssetLoader {
    
    override fun addManagedAssets(manager: AssetManager) {
        fun linearTexture(): TextureLoader.TextureParameter = TextureLoader.TextureParameter().apply {
            this.magFilter = Texture.TextureFilter.Linear
            this.minFilter = Texture.TextureFilter.Linear
        }
        
        listOf(16, 24, 32, 64, 128, 256, 512, 1024).forEach {
            AssetRegistry.loadAsset<Texture>("logo_$it", "images/icon/$it.png")
        }
        
        AssetRegistry.loadAsset<Texture>("ui_icon_palette", "images/ui/icons/palette.png", linearTexture())
        AssetRegistry.loadAsset<Texture>("ui_icon_resetwindow", "images/ui/icons/reset_window.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_fullscreen", "images/ui/icons/fullscreen.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_warn", "images/ui/icons/warn.png", linearTexture())
        AssetRegistry.loadAsset<Texture>("ui_icon_language", "images/ui/icons/language.png", linearTexture())
        AssetRegistry.loadAsset<Texture>("ui_icon_back", "images/ui/icons/back.png", linearTexture())
        AssetRegistry.loadAsset<Texture>("ui_icon_uncheckedbox", "images/ui/checkbox/unchecked.png", linearTexture())
        AssetRegistry.loadAsset<Texture>("ui_icon_checkedbox", "images/ui/checkbox/checked.png", linearTexture())
        AssetRegistry.loadAsset<Texture>("ui_icon_xcheckedbox", "images/ui/checkbox/x.png", linearTexture())
        AssetRegistry.loadAsset<Texture>("ui_icon_pencil", "images/ui/icons/pencil.png", linearTexture())
        AssetRegistry.loadAsset<Texture>("ui_icon_x", "images/ui/icons/x.png", linearTexture())
        AssetRegistry.loadAsset<Texture>("ui_icon_unfullscreen", "images/ui/icons/unfullscreen.png")
        AssetRegistry.loadAsset<Texture>("ui_stripe_board", "images/ui/stripe_board.png", linearTexture())
        AssetRegistry.loadAsset<Texture>("ui_transparent_checkerboard", "images/ui/transparent_checkerboard.png")
        AssetRegistry.loadAsset<Texture>("ui_colour_picker_arrow", "images/ui/colour_picker_arrow.png", linearTexture())
        AssetRegistry.loadAsset<Texture>("ui_textbox", "images/ui/textbox.png")
        
        AssetRegistry.loadAsset<Texture>("gear", "images/gear.png")
        AssetRegistry.loadAsset<Texture>("circle", "images/circle.png")
        AssetRegistry.loadAsset<Texture>("hexagon", "images/hexagon.png")
    }
    
    override fun addUnmanagedAssets(assets: MutableMap<String, Any>) {        
        assets["cursor_horizontal_resize"] =
                Gdx.graphics.newCursor(Pixmap(Gdx.files.internal("images/cursor/horizontal_resize.png")),
                                       16, 8)
        assets["cursor_invisible"] =
                Gdx.graphics.newCursor(Pixmap(Gdx.files.internal("images/cursor/invisible.png")), 1, 1)
    }
    
}