package mcjty.mymod.tank;

import mcjty.mymod.ModBlocks;
import mcjty.mymod.tools.IRestorableTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;

public class TileTank extends TileEntity implements IRestorableTileEntity {

    public static final int MAX_CONTENTS = 10000;       // 10 buckets

    public TileTank() {
        super(ModBlocks.TYPE_TANK);
    }

    private FluidTank tank = new FluidTank(MAX_CONTENTS) {
        @Override
        protected void onContentsChanged() {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            markDirty();
        }
    };

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbtTag = super.getUpdateTag();
        NBTTagCompound tankNBT = new NBTTagCompound();
        tank.writeToNBT(tankNBT);
        nbtTag.setTag("tank", tankNBT);
        return nbtTag;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        tank.readFromNBT(packet.getNbtCompound().getCompound("tank"));
    }


    @Override
    public void read(NBTTagCompound tagCompound) {
        super.read(tagCompound);
        readRestorableFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        writeRestorableToNBT(compound);
        return super.write(compound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound compound) {
        tank.readFromNBT(compound.getCompound("tank"));
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound compound) {
        NBTTagCompound tankNBT = new NBTTagCompound();
        tank.writeToNBT(tankNBT);
        compound.setTag("tank", tankNBT);
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> (T) tank);
        }
        return super.getCapability(capability, facing);
    }

}
