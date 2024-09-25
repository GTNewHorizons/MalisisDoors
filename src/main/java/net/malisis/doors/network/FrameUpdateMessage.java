package net.malisis.doors.network;

import net.malisis.core.network.MalisisMessage;
import net.malisis.core.util.BlockState;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.tileentity.MultiTile;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

@MalisisMessage
public class FrameUpdateMessage implements IMessageHandler<FrameUpdateMessage.Packet, IMessage> {

    public FrameUpdateMessage() {
        MalisisDoors.network.registerMessage(this, FrameUpdateMessage.Packet.class, Side.SERVER);
    }

    @Override
    public IMessage onMessage(FrameUpdateMessage.Packet message, MessageContext ctx) {
        World world = ctx.getServerHandler().playerEntity.worldObj;
        TileEntity tileEntity = world.getTileEntity(message.blockX, message.blockY, message.blockZ);
        BlockState blockState = new BlockState(
            message.blockX,
            message.blockY,
            message.blockZ,
            message.block,
            message.blockDamage);
        if (tileEntity instanceof MultiTile bigDoorTE) {
            bigDoorTE.setFrameState(blockState);
        }
        return null;
    }

    public static void SendFrameUpdateMessage(TileEntity te, Block block, int damage) {
        FrameUpdateMessage.Packet packet = new FrameUpdateMessage.Packet(
            block,
            te.xCoord,
            te.yCoord,
            te.zCoord,
            damage);
        MalisisDoors.network.sendToServer(packet);
    }

    public static class Packet implements IMessage {

        private int blockX;
        private int blockY;
        private int blockZ;
        private Block block;
        private int blockDamage;

        public Packet() {}

        public Packet(Block block, int x, int y, int z, int damage) {
            this.block = block;
            this.blockX = x;
            this.blockY = y;
            this.blockZ = z;
            this.blockDamage = damage;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.blockX = buf.readInt();
            this.blockY = buf.readInt();
            this.blockZ = buf.readInt();
            this.block = Block.getBlockById(buf.readInt());
            this.blockDamage = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(this.blockX);
            buf.writeInt(this.blockY);
            buf.writeInt(this.blockZ);
            buf.writeInt(Block.getIdFromBlock(this.block));
            buf.writeInt(this.blockDamage);
        }
    }
}
