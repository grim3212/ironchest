/*******************************************************************************
 * Copyright (c) 2012 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     cpw - initial API and implementation
 ******************************************************************************/
package cpw.mods.ironchest;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockIronChest extends BlockContainer
{
    public static final PropertyEnum VARIANT_PROP = PropertyEnum.create("variant", IronChestType.class);

    public BlockIronChest()
    {
        super(Material.iron);
        
        this.setDefaultBlockState(this.blockState.getBaseState().withProperty(VARIANT_PROP, IronChestType.IRON));
        
        this.setBlockBounds(0.0625F, 0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
        this.setHardness(3.0F);
        this.setUnlocalizedName("IronChest");
        this.setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }
    
    @Override
    public int getRenderType()
    {
        return 2;
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState blockState, EntityPlayer player, EnumFacing direction, float p_180639_6_, float p_180639_7_, float p_180639_8_)
    {
        TileEntity te = world.getTileEntity(pos);

        if (te == null || !(te instanceof TileEntityIronChest))
        {
            return true;
        }

        /*if (world.isSideSolid(i, j + 1, k, ForgeDirection.DOWN))
        {
            return true;
        }*/

        if (world.isRemote)
        {
            return true;
        }

        player.openGui(IronChest.instance, ((TileEntityIronChest) te).getType().ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
    
    @Override
    public TileEntity createNewTileEntity(World world, int metadata)
    {
        return IronChestType.makeEntity(metadata);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list)
    {
        for (IronChestType type : IronChestType.values())
        {
            if (type.isValidForCreativeMode())
            {
                list.add(new ItemStack(itemIn, 1, type.ordinal()));
            }
        }
    }
    
    @Override
    public IBlockState getBlockStateFromMeta(int meta)
    {
        return this.getDefaultBlockState().withProperty(VARIANT_PROP, IronChestType.values()[meta]);
    }

    @Override
    public int getMetaFromBlockState(IBlockState blockState)
    {
        return ((IronChestType)blockState.getValue(VARIANT_PROP)).ordinal();
    }
    
    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] { VARIANT_PROP });
    }

    /*@Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> items = Lists.newArrayList();
        ItemStack stack = new ItemStack(this,1,metadata);
        IronChestType.values()[IronChestType.validateMeta(metadata)].adornItemDrop(stack);
        items.add(stack);
        return items;
    }*/

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState blockState)
    {
        super.onBlockAdded(world, pos, blockState);
        world.markBlockForUpdate(pos);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState blockState, EntityLivingBase entityliving, ItemStack itemStack)
    {
        byte chestFacing = 0;
        int facing = MathHelper.floor_double((double) ((entityliving.rotationYaw * 4F) / 360F) + 0.5D) & 3;
        if (facing == 0)
        {
            chestFacing = 2;
        }
        if (facing == 1)
        {
            chestFacing = 5;
        }
        if (facing == 2)
        {
            chestFacing = 3;
        }
        if (facing == 3)
        {
            chestFacing = 4;
        }
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof TileEntityIronChest)
        {
            TileEntityIronChest teic = (TileEntityIronChest) te;
            teic.wasPlaced(entityliving, itemStack);
            teic.setFacing(chestFacing);
            world.markBlockForUpdate(pos);
        }
    }

    /*@Override
    public int damageDropped(int i)
    {
        return i;
    }*/

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState blockState)
    {
        TileEntityIronChest tileentitychest = (TileEntityIronChest) world.getTileEntity(pos);
        if (tileentitychest != null)
        {
            tileentitychest.removeAdornments();
            dropContent(0, tileentitychest, world, tileentitychest.getPos());
        }
        super.breakBlock(world, pos, blockState);
    }

    public void dropContent(int newSize, IInventory chest, World world, BlockPos pos)
    {
        Random random = world.rand;
        
        for (int l = newSize; l < chest.getSizeInventory(); l++)
        {
            ItemStack itemstack = chest.getStackInSlot(l);
            if (itemstack == null)
            {
                continue;
            }
            float f = random.nextFloat() * 0.8F + 0.1F;
            float f1 = random.nextFloat() * 0.8F + 0.1F;
            float f2 = random.nextFloat() * 0.8F + 0.1F;
            while (itemstack.stackSize > 0)
            {
                int i1 = random.nextInt(21) + 10;
                if (i1 > itemstack.stackSize)
                {
                    i1 = itemstack.stackSize;
                }
                itemstack.stackSize -= i1;
                EntityItem entityitem = new EntityItem(world, (float) pos.getX() + f, (float) pos.getY() + (newSize > 0 ? 1 : 0) + f1, (float) pos.getZ() + f2,
                        new ItemStack(itemstack.getItem(), i1, itemstack.getMetadata()));
                float f3 = 0.05F;
                entityitem.motionX = (float) random.nextGaussian() * f3;
                entityitem.motionY = (float) random.nextGaussian() * f3 + 0.2F;
                entityitem.motionZ = (float) random.nextGaussian() * f3;
                if (itemstack.hasTagCompound())
                {
                    entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
                }
                world.spawnEntityInWorld(entityitem);
            }
        }
    }

    /*@Override
    public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ)
    {
       TileEntity te = world.getTileEntity(x, y, z);
       if (te instanceof TileEntityIronChest)
       {
           TileEntityIronChest teic = (TileEntityIronChest) te;
           if (teic.getType().isExplosionResistant())
           {
               return 10000f;
           }
       }
       return super.getExplosionResistance(par1Entity, world, x, y, z, explosionX, explosionY, explosionZ);
    }*/

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IInventory)
        {
            return Container.calcRedstoneFromInventory((IInventory)te);
        }
        return 0;
    }

    /*private static final ForgeDirection[] validRotationAxes = new ForgeDirection[] { UP, DOWN };
    @Override
    public ForgeDirection[] getValidRotations(World worldObj, int x, int y, int z)
    {
        return validRotationAxes;
    }

    @Override
    public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis)
    {
        if (worldObj.isRemote)
        {
            return false;
        }
        if (axis == UP || axis == DOWN)
        {
            TileEntity tileEntity = worldObj.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityIronChest) {
                TileEntityIronChest icte = (TileEntityIronChest) tileEntity;
                icte.rotateAround(axis);
            }
            return true;
        }
        return false;
    }*/

}
