package com.anytypeio.anytype.ui.objects

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.databinding.FragmentObjectBinding

class ObjectFragment : Fragment(R.layout.fragment_object) {

    private var _binding: FragmentObjectBinding? = null
    private val binding get() = _binding!!

    private var lightStatusBars: Boolean = true
    private var coverVisible: Boolean = true
    private var imePaddingEnabled: Boolean = true
    private var lastInsets: WindowInsetsCompat? = null

    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentObjectBinding.bind(view)

        // Temporary: show recycler to verify layout
        // --- Demo Recycler for refactor bring-up ---
        val demoAdapter = DemoAdapter()
        binding.recycler.adapter = demoAdapter
        binding.recycler.visibility = View.VISIBLE
        demoAdapter.submitList(
            listOf(
                "Title block",
                "Paragraph block",
                "Checklist block",
                "Code block",
                "Divider",
                "Callout",
                "Quote"
            )
        )

        // Enable edge-to-edge for system bars
        val window = requireActivity().window
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        // Make status bar icons dark on light backgrounds (toggle as needed when testing)
        val controller = androidx.core.view.WindowInsetsControllerCompat(window, binding.root)
        controller.isAppearanceLightStatusBars = true

        binding.topEdgeCover.setBackgroundColor(Color.parseColor("#FF00AA")) // magenta
        binding.topEdgeCover.visibility = if (coverVisible) View.VISIBLE else View.GONE

        // Apply WindowInsets to size the top cover to the status bar height and
        // provide bottom padding for the sheet (recycler + toolbars) vs nav/IME.
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val status = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars())
            val nav = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
            val ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime())

            // Extend topToolbar to include status bar area
            binding.topToolbar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height += status.top
            }
            // Add top padding to topToolbar content to push it below status bar
            binding.topToolbar.setPadding(
                binding.topToolbar.paddingLeft,
                status.top,
                binding.topToolbar.paddingRight,
                binding.topToolbar.paddingBottom
            )

            // Pad the content bottom by the largest of nav bar or IME
            val bottom = maxOf(nav.bottom, ime.bottom)
            binding.sheet.setPadding(
                binding.sheet.paddingLeft,
                binding.sheet.paddingTop,
                binding.sheet.paddingRight,
                bottom
            )

            insets
        }

        //testing
        binding.debugFab.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menu.add(0, 1, 0, "Light status bars: toggle")
            popup.menu.add(0, 2, 1, "Top cover: show/hide")
            popup.menu.add(0, 3, 2, "Top cover: randomize color")
            popup.menu.add(0, 4, 3, "Bottom padding: toggle IME/nav")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        lightStatusBars = !lightStatusBars
                        val controller = WindowInsetsControllerCompat(requireActivity().window, binding.root)
                        controller.isAppearanceLightStatusBars = lightStatusBars
                        true
                    }
                    2 -> {
                        coverVisible = !coverVisible
                        binding.topEdgeCover.visibility = if (coverVisible) View.VISIBLE else View.GONE
                        true
                    }
                    3 -> {
                        val rnd = kotlin.random.Random(System.currentTimeMillis())
                        val color = android.graphics.Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                        binding.topEdgeCover.setBackgroundColor(color)
                        true
                    }
                    4 -> {
                        imePaddingEnabled = !imePaddingEnabled
                        // re-apply latest insets immediately
                        lastInsets?.let { insets ->
                            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
                            val bottom = if (imePaddingEnabled) maxOf(nav.bottom, ime.bottom) else 0
                            binding.sheet.setPadding(
                                binding.sheet.paddingLeft,
                                binding.sheet.paddingTop,
                                binding.sheet.paddingRight,
                                bottom
                            )
                        }
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}


// --- Minimal demo adapter to visualize the refactored layout ---
private class DemoAdapter : ListAdapter<String, DemoVH>(DIFF) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_block_title, parent, false)
        return DemoVH(view)
    }

    override fun onBindViewHolder(holder: DemoVH, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem === newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    }
}

private class DemoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(title: String) {
        // `item_block_title` is assumed to contain a TextView with id `title`
        itemView.findViewById<TextView>(com.anytypeio.anytype.R.id.title)?.text = title
    }
}