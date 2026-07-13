package mx.kenzie.survival.potion;

import org.jetbrains.annotations.ApiStatus;

public class Later<Type> {

    protected Type value;
    protected boolean present;

    @ApiStatus.Internal
    public Later(Type initial, boolean present) {
        this.value = initial;
        this.present = present;
    }

    public Later(Type value) {
        this.initialiseUnsafe(value);
    }

    public Later() {
    }

    public void initialise(Type value) {
        if (present) throw new IllegalStateException("Cannot initialise more than once");
        this.initialiseUnsafe(value);
        this.value = value;
        this.present = true;
    }

    @ApiStatus.Internal
    public void initialiseUnsafe(Type value) {
        this.value = value;
        this.present = true;
    }

    public boolean isPresent() {
        return present;
    }

}
