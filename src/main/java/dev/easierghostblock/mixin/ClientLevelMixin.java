package dev.easierghostblock.mixin;

import dev.easierghostblock.AckDelayQueue;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When you break/place a block the client predicts the result immediately and remembers it
 * under a sequence number. While that prediction is pending, block updates the server sends
 * for that position are only recorded, not shown. The server's "block changed ack" packet
 * ends the prediction and, if the server disagreed, snaps the block back - that is the moment
 * a ghost block disappears. Delaying that ack keeps the ghost block around for longer.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "handleBlockChangedAck", at = @At("HEAD"), cancellable = true)
    private void easierghostblock$delayAck(int sequence, CallbackInfo ci) {
        if (AckDelayQueue.tryDelay((ClientLevel) (Object) this, sequence)) {
            ci.cancel();
        }
    }
}
