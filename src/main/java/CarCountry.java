enum CarCountry
{
    NL("Netherlands")
    , IT("Italy")
    , ES("Spain")
    , SE("Sweden")
    , OTHER("Inny");

    private final String name;

    CarCountry(final String name)
    {
        this.name = name;
    }

    String getName()
    {
        return this.name;
    }
}
