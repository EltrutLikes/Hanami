package com.teamaurora.hanami.common.entity.block;

import com.teamaurora.hanami.common.entity.SakuraBlossomEntity;
import com.teamaurora.hanami.core.registry.HanamiEntities;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.*;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class BlombEntity extends TNTEntity {
    private static final DataParameter<Integer> FUSE = EntityDataManager.createKey(BlombEntity.class, DataSerializers.VARINT);
    @Nullable
    private LivingEntity tntPlacedBy;
    private int fuse = 80;

    public BlombEntity(EntityType<? extends BlombEntity> type, World worldIn) {
        super(type, worldIn);
        this.preventEntitySpawning = true;
    }

    public BlombEntity(World worldIn, double x, double y, double z, @Nullable LivingEntity igniter) {
        this(HanamiEntities.BLOMB.get(), worldIn);
        this.setPosition(x, y, z);
        double d0 = worldIn.rand.nextDouble() * (double)((float)Math.PI * 2F);
        this.setMotion(-Math.sin(d0) * 0.02D, 0.2F, -Math.cos(d0) * 0.02D);
        this.setFuse(80);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.tntPlacedBy = igniter;
    }

    protected void registerData() {
        super.registerData();
        this.dataManager.register(FUSE, 80);
    }

    @Override
    public void tick() {
        if (!this.hasNoGravity()) {
            this.setMotion(this.getMotion().add(0.0D, -0.04D, 0.0D));
        }

        this.move(MoverType.SELF, this.getMotion());
        this.setMotion(this.getMotion().scale(0.98D));
        if (this.onGround) {
            this.setMotion(this.getMotion().mul(0.7D, -0.5D, 0.7D));
        }

        --this.fuse;
        if (this.fuse <= 0) {
            this.remove();
            if (!this.world.isRemote) {
                this.yeet();
                this.world.playSound(null, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        } else {
            this.func_233566_aG_();
            if (this.world.isRemote) {
                this.world.addParticle(ParticleTypes.SMOKE, this.getPosX(), this.getPosY() + 0.5D, this.getPosZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public void yeet() {
        //TODO
        AxisAlignedBB explosionBB = new AxisAlignedBB(this.getPositionVec().add(-3, -3, -3), this.getPositionVec().add(3, 3, 3));
        List<Entity> entitiesAbove = this.world.getEntitiesWithinAABBExcludingEntity(null, explosionBB);
        if(!entitiesAbove.isEmpty()) {
            /*int j = entitiesAbove.size();
            float[] healths = new float[j];
            EffectInstance[] resistances = new EffectInstance[j];
            boolean[] resActive = new boolean[j];
            boolean[] invulnerables = new boolean[j];
            for (int i = 0; i < j; i++) {
                resActive[i] = false;
                Entity entity = entitiesAbove.get(i);
                if (!(entity instanceof BlombEntity)) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity) entity;
                        /*healths[i] = living.getHealth();
                        if (living.isPotionActive(Effects.RESISTANCE)) {
                            resActive[i] = true;
                            resistances[i] = living.getActivePotionEffect(Effects.RESISTANCE);
                        }
                        living.removePotionEffect(Effects.RESISTANCE);
                        living.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 1, 100));*/
                        /*invulnerables[i] = living.isInvulnerable();
                        living.setInvulnerable(true);
                    }
                }
            }
            this.world.createExplosion(this, this.getPosX(), this.getPosYHeight(0.0625D), this.getPosZ(), 4.0F, Explosion.Mode.NONE);
            for (int i = 0; i < j; i++) {
                Entity entity = entitiesAbove.get(i);
                if (!(entity instanceof BlombEntity)) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity) entity;
                        //living.setHealth(healths[i]);
                        /*living.removePotionEffect(Effects.RESISTANCE);
                        if (resActive[i]) {
                            living.addPotionEffect(resistances[i]);
                        }*/
                        /*living.setInvulnerable(invulnerables[i]);
                    }
                }
            }*/

            // this is *very* hacky but hopefully it'll work
        }/* else {
            this.world.createExplosion(this, this.getPosX(), this.getPosYHeight(0.0625D), this.getPosZ(), 0.0F, Explosion.Mode.NONE);
        }*/
        if (this.world.isRemote) {
            this.world.playSound(this.getPosX(), this.getPosYHeight(0.0625), this.getPosZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F, false);
        }
        this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getPosX(), this.getPosYHeight(0.0625), this.getPosZ(), 1.0D, 0.0D, 0.0D);
    }

    protected void writeAdditional(CompoundNBT compound) {
        compound.putShort("Fuse", (short)this.getFuse());
    }

    protected void readAdditional(CompoundNBT compound) {
        this.setFuse(compound.getShort("Fuse"));
    }

    @Nullable
    public LivingEntity getTntPlacedBy() {
        return this.tntPlacedBy;
    }

    protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return 0.0F;
    }

    public void notifyDataManagerChange(DataParameter<?> key) {
        if (FUSE.equals(key)) {
            this.fuse = this.getFuseDataManager();
        }
    }

    public int getFuseDataManager() {
        return this.dataManager.get(FUSE);
    }

    public int getFuse() {
        return this.fuse;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}