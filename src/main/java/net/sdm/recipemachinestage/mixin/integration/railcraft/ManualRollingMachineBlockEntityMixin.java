package net.sdm.recipemachinestage.mixin.integration.railcraft;

import mods.railcraft.world.item.crafting.RailcraftRecipeTypes;
import mods.railcraft.world.item.crafting.RollingRecipe;
import mods.railcraft.world.level.block.entity.ManualRollingMachineBlockEntity;
import net.sdm.recipemachinestage.SupportBlockData;
import net.sdm.recipemachinestage.capability.IOwnerBlock;
import net.sdm.recipemachinestage.stage.StageContainer;
import net.sdm.recipemachinestage.stage.type.RecipeBlockType;
import net.sdm.recipemachinestage.utils.PlayerHelper;
import net.sdm.recipemachinestage.utils.RecipeStagesUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = ManualRollingMachineBlockEntity.class, remap = false)
public class ManualRollingMachineBlockEntityMixin {

    private ManualRollingMachineBlockEntity thisEntity = RecipeStagesUtil.cast(this);

    @Inject(method = "getRecipe", at = @At("RETURN"))
    public void sdm$getRecipe(CallbackInfoReturnable<Optional<RollingRecipe>> cir){
        if(StageContainer.INSTANCE.RECIPES_STAGES.isEmpty() || !StageContainer.INSTANCE.RECIPES_STAGES.containsKey(RailcraftRecipeTypes.ROLLING.get())) return;

        Optional<RollingRecipe> recipeOptional = cir.getReturnValue();
        if(recipeOptional.isPresent()) {
            var recipe = recipeOptional.get();
            Optional<IOwnerBlock> d1 = thisEntity.getCapability(SupportBlockData.BLOCK_OWNER).resolve();
            if (d1.isPresent() && thisEntity.getLevel().getServer() != null) {
                IOwnerBlock ownerBlock = d1.get();
                RecipeBlockType recipeBlockType =  StageContainer.getRecipeData(recipe.getType(), recipe.getId());
                if(recipeBlockType != null) {
                    PlayerHelper.@Nullable RMSStagePlayerData player = PlayerHelper.getPlayerByGameProfile(thisEntity.getLevel().getServer(), ownerBlock.getOwner());
                    if(player != null) {
                        if(!player.hasStage(recipeBlockType.stage)) {
                            cir.cancel();
                        }
                    }
                }
            }
        }
    }
}