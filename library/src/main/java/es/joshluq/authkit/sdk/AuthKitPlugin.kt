package es.joshluq.authkit.sdk

/**
 * Interface for AuthKit plugins.
 *
 * @param TConfig The configuration type for the plugin.
 * @param TInstance The instance type that the plugin provides.
 */
interface AuthKitPlugin<TConfig : Any, TInstance : Any> {
    /**
     * Installs the plugin into the [AuthKit] instance.
     *
     * @param authKit The AuthKit instance.
     * @param config The plugin configuration.
     * @return The plugin instance.
     */
    fun install(authKit: AuthKit, config: TConfig): TInstance
}
