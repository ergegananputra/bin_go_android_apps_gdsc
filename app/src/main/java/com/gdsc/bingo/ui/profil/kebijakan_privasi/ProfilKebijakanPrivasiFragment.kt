package com.gdsc.bingo.ui.profil.kebijakan_privasi

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.gdsc.bingo.databinding.FragmentProfilKebijakanPrivasiBinding

class ProfilKebijakanPrivasiFragment : Fragment() {

    private var _binding: FragmentProfilKebijakanPrivasiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilKebijakanPrivasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userPolicyText = """
            <br><br>
            <div style="text-align: center;"><h2><b>User Policy for BINGO</b></h2></div><br><br>
            
            <div class="policy-text">
                <b>Welcome to BINGO!</b> We are delighted to have you as part of our community. Before you start using our app, please take a moment to review our user policy. By accessing or using BINGO, you agree to comply with this policy.<br><br>
            
                <b>1. Account Registration:</b><br>
                - To access certain features of BINGO, you may need to create an account. When registering, you agree to provide accurate and complete information.<br>
                - You are responsible for maintaining the confidentiality of your account credentials and for all activities that occur under your account.<br><br>
            
                <b>2. App Usage:</b><br>
                - BINGO is designed to promote responsible waste disposal practices and community engagement in waste management initiatives.<br>
                - Users must not use BINGO for any unlawful, harmful, or inappropriate purposes. This includes but is not limited to: posting offensive content, engaging in illegal activities, or violating the rights of others.<br>
                - Users are encouraged to engage respectfully with other community members and contribute constructively to discussions.<br><br>
            
                <b>3. Content Guidelines:</b><br>
                - Users are responsible for the content they post on BINGO. Content must not violate any laws or infringe upon the rights of others.<br>
                - Content that promotes hate speech, violence, discrimination, or illegal activities is strictly prohibited.<br>
                - BINGO reserves the right to remove any content that violates these guidelines without prior notice.<br><br>
            
                <b>4. Privacy and Data Security:</b><br>
                - BINGO respects your privacy and is committed to protecting your personal information. Please refer to our Privacy Policy for details on how we collect, use, and disclose your data.<br>
                - You agree not to misuse any personal information of other users obtained through BINGO.<br><br>
            
                <b>5. Reporting Violations:</b><br>
                - If you encounter any violations of this user policy or observe inappropriate behavior on BINGO, please report it to us immediately.<br>
                - BINGO will investigate reported violations and take appropriate action, including removing offensive content and suspending or terminating accounts.<br><br>
            
                <b>6. Changes to Policy:</b><br>
                - BINGO reserves the right to update or modify this user policy at any time without prior notice. Any changes will be effective immediately upon posting.<br>
                - It is your responsibility to review this policy periodically to stay informed of any updates.<br><br>
            
                Thank you for being part of the BINGO community! If you have any questions or concerns about this user policy, please contact us at <a href='mailto:bingo@gmail.com'>bingo@gmail.com</a>.
            </div>
            """.trimIndent()

        binding.textViewUserPolicy.text = HtmlCompat.fromHtml(userPolicyText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}