package com.anytypeio.anytype.ui.objects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.databinding.FragmentObjectBinding

class ObjectFragment : Fragment(R.layout.fragment_object) {

    private var _binding: FragmentObjectBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentObjectBinding.bind(view)

        // Temporary: show recycler to verify layout
        binding.recycler.visibility = View.VISIBLE

        val adapter = DemoAdapter()
        binding.recycler.adapter = adapter
        adapter.submitList(listOf("Heading", "Paragraph", "Checklist", "Code Block"))

        // Enable edge-to-edge for system bars
        val window = requireActivity().window
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        // Make status bar icons dark on light backgrounds (toggle as needed when testing)
        val controller = androidx.core.view.WindowInsetsControllerCompat(window, binding.root)
        controller.isAppearanceLightStatusBars = true

        // Give the top cover a random background so changes are obvious during testing
        val rnd = kotlin.random.Random(System.currentTimeMillis())
        val color =
            android.graphics.Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        binding.topEdgeCover.setBackgroundColor(color)

        // Apply WindowInsets to size the top cover to the status bar height and
        // provide bottom padding for the sheet (recycler + toolbars) vs nav/IME.
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val status = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars())
            val nav = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
            val ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime())

            // Match the top cover height to status bar height
            binding.topEdgeCover.layoutParams = binding.topEdgeCover.layoutParams.apply {
                height = status.top
            }
            binding.topEdgeCover.requestLayout()

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
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}


private class DemoAdapter : ListAdapter<String, DemoVH>(diff) {
    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        DemoVH(LayoutInflater.from(p.context).inflate(R.layout.item_block_title, p, false))

    override fun onBindViewHolder(h: DemoVH, i: Int) = h.bind(getItem(i))

    companion object {
        private val diff = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(a: String, b: String) = a === b
            override fun areContentsTheSame(a: String, b: String) = a == b
        }
    }
}

private class DemoVH(v: View) : RecyclerView.ViewHolder(v) {
    fun bind(title: String) {
        // Bind to your title TextView inside item_block_title
        itemView.findViewById<TextView>(R.id.title)?.text = title
    }
}