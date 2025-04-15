package net.dv8tion.jda.api.entities;

public enum SKUType
{
    DURABLE(2),
    CONSUMABLE(3),
    SUBSCRIPTION(5),
    SUBSCRIPTION_GROUP(6);

    private final int id;

    SKUType(int id)
    {
        this.id = id;
    }

    public int id()
    {
        return id;
    }
}
