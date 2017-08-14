package mod.render360.coretransform.classtransformers;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import mod.render360.coretransform.CLTLog;
import mod.render360.coretransform.RenderUtil;
import mod.render360.coretransform.classtransformers.name.ClassName;
import mod.render360.coretransform.classtransformers.name.MethodName;
import mod.render360.coretransform.classtransformers.name.Names;
import mod.render360.coretransform.render.RenderMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.shader.Framebuffer;

import static org.objectweb.asm.Opcodes.*;

public class LoadingScreenRendererTransformer extends ClassTransformer {

	@Override
	public ClassName getClassName() {return Names.LoadingScreenRenderer;}

	@Override
	public MethodTransformer[] getMethodTransformers() {
		
		MethodTransformer transformSetLoadingProgress = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.LoadingScreenRenderer_setLoadingProgress;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					if (instruction.getOpcode() == FSTORE) {
						CLTLog.info("Found FSTORE in method " + getMethodName().debug());
						
						for (int i = 0; i < 3; i++) {
							instruction = instruction.getNext();
						}
						
						//if (RenderUtil.renderMethod.replaceLoadingScreen()) {
						//	RenderUtil.renderMethod.renderLoadingScreen(this.mc.guiScreen, framebuffer);
						//} else {
						//vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
						//...
						//tessellator.draw();
						//}
						
						InsnList toInsert = new InsnList();
						LabelNode label1 = new LabelNode();
						LabelNode label2 = new LabelNode();
						
						//if (RenderUtil.renderMethod.replaceLoadingScreen())
						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class),
								"renderMethod", "L" + Type.getInternalName(RenderMethod.class) + ";")); //renderMethod
						toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(RenderMethod.class),
								"replaceLoadingScreen", "()Z", false)); //replaceLoadingScreen()
						toInsert.add(new JumpInsnNode(IFEQ, label1));
						
						//RenderUtil.renderMethod.renderLoadingScreen(this.mc.guiScreen, framebuffer);
						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class),
								"renderMethod", "L" + Type.getInternalName(RenderMethod.class) + ";")); //renderMethod
						
						toInsert.add(new VarInsnNode(ALOAD, 0)); //this
						toInsert.add(new FieldInsnNode(GETFIELD, classNode.name,
								Names.LoadingScreenRenderer_mc.getFullName(),
								Names.LoadingScreenRenderer_mc.getDesc())); //mc
						toInsert.add(new FieldInsnNode(GETFIELD, Names.Minecraft.getInternalName(obfuscated),
								Names.Minecraft_currentScreen.getFullName(),
								Names.Minecraft_currentScreen.getDesc())); //currentScreen
						
						toInsert.add(new VarInsnNode(ALOAD, 0)); //this
						toInsert.add(new FieldInsnNode(GETFIELD, classNode.name,
								Names.LoadingScreenRenderer_framebuffer.getFullName(),
								Names.LoadingScreenRenderer_framebuffer.getDesc())); //framebuffer
						toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(RenderMethod.class),
								"renderLoadingScreen", "(L" + Names.GuiScreen.getInternalName(obfuscated) +
								";L" + Names.Framebuffer.getInternalName(obfuscated) + ";)V", false));
						
						//else
						toInsert.add(new JumpInsnNode(GOTO, label2));
						toInsert.add(label1);
						
						method.instructions.insertBefore(instruction, toInsert);
						
						//go to tessellator.draw();
						instruction = method.instructions.get(
								method.instructions.indexOf(instruction)+91);
						method.instructions.insert(instruction, label2);
						
						break;
					}
				}
			}
		};
		
		return new MethodTransformer[] {transformSetLoadingProgress};
	}

}
