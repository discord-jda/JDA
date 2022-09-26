# Entity Mixins
Entity Mixins are a system in JDA that provide the following core functionalities:
- Code reuse through composition instead of purely through inheritence
- A way to expose internal state setters and getters without exposing them to the library user

## Example Mixin with Usage
```java
//Publicly exposed entity api interface
public interface SomeEntity {
    String getName();
    
    default String getNameTwice() {
      return getName() + "-" + getName();
    }

    RestAction<Void> updateName(String name);
}

//Internal mixin for that entity for code reuse and state exposing
public interface SomeEntityMixin<T extends SomeEntityMixin<T>> extends SomeEntity {
    //---- Default implementations of interface ----
    @Override
    default RestAction<Void> updateName(String name) {
        checkCanModifyEntity();
        
        Route.CompiledRoute route = Route.custom(Method.POST, "/someEntity/name/");
        return new RestActionImpl<>(route);
    }
    
    //---- State Accessors ----
    T setName(String name);
    
    //---- Mixin Hooks -----
    void checkCanModifyEntity();
}

//Internal concrete implementation of the entity
public class SomeEntityImpl implements SomeEntityMixin<SomeEntityImpl> {
    private String name;
    
    public SomeEntityImpl() {}
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public SomeEntityImpl setName(String name) {
        this.name = name;
    }
    
    @Override
    public void checkCanModifyEntity() {
        //Do some check here, throw if check is bad!
    }
}

```

## Mixin Anatomy
Table of Contents
- Default implementations of interface
- Default implementation of parent mixins hooks
- State Accessors
- Mixin Hooks
- Helpers

### `---- Default implementations of interface ----`
These consist of the `default` implementation of any methods that aren't already implemented via `default` in the
interface itself that can be implemented via any of the `State Accessors` or `Mixin Hooks` added. Basically everything in the interface
should be able to be implemented here. The only things that will not be implemented are basic getter methods that 
map _directly_ to underlying state with 0 modification. 
Example:
```java
int getBitrate();
```

This getter will map _directly_ to the `int bitrate` field of the underlying class and there's nothing we can do to 
implement this in the mixin.

In contrast, a _calculated_ getter (or any other methods) can and should be implemented via the exposing of a
`State Accessor` or `Mixin Hook`. For example:
```java
// ---- Default implementations of interface ----
default List<PermissionOverride> getPermissionOverrides() {
  TLongObjectMap<PermissionOverride> overrides = getPermissionOverrideMap();
  return Arrays.asList(overrides.values(new PermissionOverride[overrides.size()]));
}

// ---- State Accessors ----
TLongObjectMap<PermissionOverride> getPermissionOverrideMap();
```

### `---- Default implementation of parent mixins hooks ----`
These are very similar to the `Default implementations of interface` methods, however, instead of providing `default`
implementations for the selected interface they are providing `default` implementations of `Mixin Hooks` defined by
any of the parent mixins that this mixin may be extending.

The same rules that apply to `Default implementations of interface` apply to these, they just live in a different
section to better drill home where they're coming from.

### `---- State Accessors ----`
State accessors are hooks that the mixin defines that give it access to the underlying state that is implemented in 
the concrete object so that the mixin can implement additional `default` functionality that would rely on accessing 
the state in the concrete object.

For example, the `IPermissionContainerMixin` implements the `IPermissionContainer#getPermissionOverrides` method via
usage of a state accessor to access the underlying map. See the example in `Default implementations of interface` as it
shows the expected State Accessor declaration.

This is what would be expected to be implemented by the concrete channel type that uses the `IPermissionContainerMixin`
```java
TLongObjectMap<PermissionOverride> overrides = MiscUtil.newLongMap();

@Override
public TLongObjectMap<PermissionOverride> getPermissionOverrideMap() {
  return overrides;
}
```

Additionally, state accessors provide a way to expose interface-specific setters or getters used outside the mixin.
This can include setters and getters for updating fields when entities update, or underlying maps when relationships update.
For example, the `AudioChannelMixin` exposes: 
```java
TLongObjectMap<Member> getConnectedMembersMap();
```
It does this so that methods that handle people joining and leaving audio channels can properly update the connected user
mapping without needing to know the underlying channel type.

### `---- Mixin Hooks ----`
Mixin Hooks are very similar to State Accessors. The difference being that while they are methods that must be implemented
by subclasses of the mixin so that the mixin can provide additional `default` functionality, they aren't intended to map
directly to underlying state.

They are simply hooks that need to be filled in so that the mixin has all the information it needs to do its job.

### `---- Helpers ----`
This section of methods is just for defining helper methods used by this mixin or the mixin/classes that subclass it.
Nothing special to mention beyond that.
