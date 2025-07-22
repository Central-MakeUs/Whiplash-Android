package com.whiplash.presentation.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.whiplash.presentation.databinding.FragmentPlaceBottomSheetBinding

/**
 * 네이버 지도를 띄우는 액티비티에 표시되는 바텀 시트
 */
class PlaceBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPlaceBottomSheetBinding? = null
    private val binding
        get() = _binding!!

    // 바텀 시트에 표시될 주소, 자세한 주소
    private var address: String? = null
    private var detailAddress: String? = null

    companion object {
        private const val ARG_ADDRESS = "address"
        private const val ARG_DETAIL_ADDRESS = "detailAddress"

        fun newInstance(
            address: String,
            detailAddress: String,
        ): PlaceBottomSheetFragment = PlaceBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ADDRESS, address)
                putString(ARG_DETAIL_ADDRESS, detailAddress)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 바텀 시트 바깥 영역을 눌러도 사라지지 않게
        isCancelable = false
        arguments?.let {
            address = it.getString(ARG_ADDRESS)
            detailAddress = it.getString(ARG_DETAIL_ADDRESS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomSheetBehavior()
        setupViews()
        setupListeners()
    }

    private fun setupBottomSheetBehavior() {
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as com.google.android.material.bottomsheet.BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(sheet)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
            }
        }
    }

    private fun setupViews() {
        with(binding) {
            tvPlaceAddress.text = address
            tvPlaceDetailAddress.text = detailAddress
        }
    }

    private fun setupListeners() {
        with(binding) {
            btnCancelRegisterAddress.setOnClickListener {
                dismiss()
                requireActivity().finish()
            }

            btnRegisterAddress.setOnClickListener {
            }
        }
    }

    fun updateAddress(address: String, detailAddress: String) {
        this.address = address
        this.detailAddress = detailAddress
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
