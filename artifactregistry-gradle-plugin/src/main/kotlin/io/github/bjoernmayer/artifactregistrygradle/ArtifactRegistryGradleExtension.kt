package io.github.bjoernmayer.artifactregistrygradle

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

public open class ArtifactRegistryGradleExtension internal constructor(
    objectFactory: ObjectFactory,
) {
    internal val applicationDefaultExtension =
        ApplicationDefaultExtension(
            objectFactory,
        )

    internal val gCloudSDKExtension =
        GCloudSDKExtension(
            objectFactory,
        )

    public fun applicationDefault(action: Action<ApplicationDefaultExtension>) {
        action.execute(applicationDefaultExtension)
    }

    public fun gCloudSDK(action: Action<GCloudSDKExtension>) {
        action.execute(gCloudSDKExtension)
    }

    public abstract class CredentialsSupplierExtension(
        objectFactory: ObjectFactory,
    ) {
        public val enable: Property<Boolean> =
            objectFactory.property<Boolean> {
                set(true)
            }

        public abstract val order: Property<Byte>
    }

    public class ApplicationDefaultExtension internal constructor(
        objectFactory: ObjectFactory,
    ) : CredentialsSupplierExtension(objectFactory) {
        override val order: Property<Byte> =
            objectFactory.property<Byte> {
                set(1)
            }
    }

    public class GCloudSDKExtension internal constructor(
        objectFactory: ObjectFactory,
    ) : CredentialsSupplierExtension(objectFactory) {
        override val order: Property<Byte> =
            objectFactory.property<Byte> {
                set(2)
            }
    }

    private companion object {
        inline fun <reified T> ObjectFactory.property(configuration: Property<T>.() -> Unit = {}): Property<T> =
            property(T::class.java).apply(configuration)
    }
}
