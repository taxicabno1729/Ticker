package com.example.liveticker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.liveticker.data.CoinRepository
import com.example.liveticker.data.Resource
import com.example.liveticker.data.WalletRepository
import com.example.liveticker.databinding.FragmentFirstBinding
import com.example.liveticker.ui.TickerAdapter
import com.example.liveticker.ui.TickerViewModel
import com.example.liveticker.ui.TickerViewModelFactory
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private lateinit var tickerAdapter: TickerAdapter
    private lateinit var viewModel: TickerViewModel
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 10000L
    private var connectedAddress: String? = null

    private val appKitDelegate = object : AppKit.ModalDelegate {
        override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
            val account = AppKit.getAccount()
            if (account != null) {
                connectedAddress = account.address
            }
            activity?.runOnUiThread {
                applyConnectedState()
            }
        }

        override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
            activity?.runOnUiThread {
                Toast.makeText(context, "Wallet connection rejected", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {}
        override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {}
        override fun onSessionExtend(session: Modal.Model.Session) {}

        override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
            activity?.runOnUiThread { applyDisconnectedState() }
        }

        override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {}

        override fun onError(error: Modal.Model.Error) {
            activity?.runOnUiThread {
                Toast.makeText(context, "Connection error: ${error.throwable.message}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {}
        override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {}
        override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val coinRepository = CoinRepository()
        val walletRepository = WalletRepository()
        val factory = TickerViewModelFactory(coinRepository, walletRepository)
        viewModel = ViewModelProvider(this, factory)[TickerViewModel::class.java]

        AppKit.setDelegate(appKitDelegate)

        setupRecyclerView()
        setupSearchView()
        setupConnectButton()
        setupPortfolioButton()
        setupRetryButton()
        
        observeViewModel()

        startTickerUpdates()
        restoreSessionIfConnected()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.tickers.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        if (tickerAdapter.itemCount == 0) {
                            binding.tickerLoading.visibility = View.VISIBLE
                        }
                    }
                    is Resource.Success -> {
                        binding.tickerLoading.visibility = View.GONE
                        binding.tickerError.visibility = View.GONE
                        tickerAdapter.submitList(resource.data)
                    }
                    is Resource.Error -> {
                        binding.tickerLoading.visibility = View.GONE
                        if (tickerAdapter.itemCount == 0) {
                            binding.tickerError.visibility = View.VISIBLE
                            binding.tickerErrorText.text = resource.message
                        }
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.nativeBalance.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val balance = resource.data
                        if (balance != null) {
                            binding.walletBalance.text = "Balance: %.4f ETH".format(balance)
                            binding.walletBalance.visibility = View.VISIBLE
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Error fetching balance: ${resource.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun restoreSessionIfConnected() {
        val account = try { AppKit.getAccount() } catch (e: Exception) { null }
        if (account != null) {
            connectedAddress = account.address
            applyConnectedState()
        }
    }

    private fun applyConnectedState() {
        val address = connectedAddress ?: return
        if (_binding == null) return
        binding.walletAddress.text = address
        binding.portfolioButton.isEnabled = true
        binding.connectButton.text = getString(R.string.disconnect_wallet)
        binding.connectButton.isEnabled = true
        viewModel.fetchWalletBalance(address)
    }

    private fun applyDisconnectedState() {
        if (_binding == null) return
        connectedAddress = null
        binding.walletAddress.text = ""
        binding.portfolioButton.isEnabled = false
        binding.walletBalance.visibility = View.GONE
        binding.connectButton.text = getString(R.string.connect_wallet)
        binding.connectButton.isEnabled = true
    }

    private fun setupRecyclerView() {
        tickerAdapter = TickerAdapter { ticker ->
            val bundle = bundleOf(
                "coin_id" to ticker.id,
                "coin_name" to ticker.name,
                "coin_symbol" to ticker.symbol
            )
            findNavController().navigate(R.id.action_FirstFragment_to_CoinDetailFragment, bundle)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tickerAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterTickers(newText)
                return true
            }
        })
    }

    private fun setupConnectButton() {
        binding.connectButton.setOnClickListener {
            if (connectedAddress != null) {
                AppKit.disconnect(
                    onSuccess = {
                        activity?.runOnUiThread { applyDisconnectedState() }
                    },
                    onError = { throwable ->
                        Log.e("FirstFragment", "Disconnect error: ${throwable.message}")
                        activity?.runOnUiThread { applyDisconnectedState() }
                    }
                )
            } else {
                findNavController().navigate(R.id.appKit)
            }
        }
    }

    private fun setupPortfolioButton() {
        binding.portfolioButton.setOnClickListener {
            connectedAddress?.let { address ->
                val bundle = bundleOf("wallet_address" to address)
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
            }
        }
    }

    private fun setupRetryButton() {
        binding.tickerRetryButton.setOnClickListener {
            binding.tickerError.visibility = View.GONE
            viewModel.fetchTickers()
        }
    }

    private fun startTickerUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                viewModel.fetchTickers()
                handler.postDelayed(this, refreshInterval)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}
