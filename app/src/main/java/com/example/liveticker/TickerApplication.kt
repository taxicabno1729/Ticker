package com.example.liveticker

import android.app.Application
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.presets.AppKitChainsPresets

class TickerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val projectId = BuildConfig.REOWN_PROJECT_ID

        val appMetaData = Core.Model.AppMetaData(
            name = "Live Ticker",
            description = "Crypto portfolio tracker",
            url = "https://liveticker.example.com",
            icons = emptyList(),
            redirect = "liveticker://request"
        )

        CoreClient.initialize(
            application = this,
            projectId = projectId,
            metaData = appMetaData,
            connectionType = ConnectionType.AUTOMATIC,
            onError = { error ->
                android.util.Log.e("TickerApp", "CoreClient init error: ${error.throwable.message}")
            }
        )

        val chains = listOfNotNull(
            AppKitChainsPresets.ethChains["1"],       // Ethereum
            AppKitChainsPresets.ethChains["137"],     // Polygon
            AppKitChainsPresets.ethChains["42161"],   // Arbitrum
            AppKitChainsPresets.ethChains["10"],      // Optimism
            AppKitChainsPresets.ethChains["8453"]     // Base
        )

        AppKit.initialize(
            init = Modal.Params.Init(CoreClient),
            onSuccess = { /* ready */ },
            onError = { error ->
                android.util.Log.e("TickerApp", "AppKit init error: ${error.throwable.message}")
            }
        )

        if (chains.isNotEmpty()) {
            AppKit.setChains(chains)
        }
    }
}
