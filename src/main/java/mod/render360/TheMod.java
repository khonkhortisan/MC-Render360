package mod.render360;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = TheMod.MOD_ID, name = TheMod.MOD_NAME, version = TheMod.MOD_VERSION, useMetadata = true)
public class TheMod
{
    public static final String MOD_ID = "render360";
    public static final String MOD_NAME = "Render 360";
    public static final String MOD_VERSION = "2.3.3";
    
    public static final String RESOURCE_PREFIX = MOD_ID.toLowerCase() + ':';
    
    @Mod.Instance
    public static TheMod instance;
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());
        Log.info("" + ForgeVersion.mcVersion);
    }
}
