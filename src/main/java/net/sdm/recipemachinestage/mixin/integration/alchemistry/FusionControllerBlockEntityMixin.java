package net.sdm.recipemachinestage.mixin.integration.alchemistry;

import com.smashingmods.alchemistry.common.block.dissolver.DissolverBlockEntity;
import com.smashingmods.alchemistry.common.block.fusion.FusionControllerBlockEntity;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
import com.smashingmods.alchemistry.common.recipe.fusion.FusionRecipe;
import com.smashingmods.alchemistry.registry.RecipeRegistry;
import com.smashingmods.alchemylib.api.recipe.AbstractProcessingRecipe;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = FusionControllerBlockEntity.class, remap = false)
public class FusionControllerBlockEntityMixin {

    private FusionControllerBlockEntity thisEntity = RecipeStagesUtil.cast(this);

    @Inject(method = "setRecipe", at = @At("HEAD"), cancellable = true)
    public <R extends AbstractProcessingRecipe> void sdm$setRecipe(@Nullable R recipe, CallbackInfo ci){
        if(StageContainer.INSTANCE.RECIPES_STAGES.isEmpty() || !StageContainer.INSTANCE.RECIPES_STAGES.containsKey(RecipeRegistry.FUSION_TYPE.get())) return;

        if(!(recipe instanceof FusionRecipe)) return;

        Optional<IOwnerBlock> d1 = thisEntity.getCapability(SupportBlockData.BLOCK_OWNER).resolve();
        if (d1.isPresent() && thisEntity.getLevel().getServer() != null) {
            IOwnerBlock ownerBlock = d1.get();
            RecipeBlockType recipeBlockType =  StageContainer.getRecipeData(recipe.getType(), recipe.getId());
            if(recipeBlockType != null) {
                PlayerHelper.@Nullable RMSStagePlayerData player = PlayerHelper.getPlayerByGameProfile(thisEntity.getLevel().getServer(), ownerBlock.getOwner());
                if(player != null) {
                    if(!player.hasStage(recipeBlockType.stage)) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}