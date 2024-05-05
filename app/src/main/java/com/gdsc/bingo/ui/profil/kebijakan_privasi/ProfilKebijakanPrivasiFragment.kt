package com.gdsc.bingo.ui.profil.kebijakan_privasi

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gdsc.bingo.databinding.FragmentProfilKebijakanPrivasiBinding
import com.gdsc.bingo.services.textstyling.AddOnSpannableTextStyle

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
            
            
                <b>Welcome to BINGO!</b> We are delighted to have you as part of our community. Before you start using our app, please take a moment to review our user policy. By accessing or using BINGO, you agree to comply with this policy.<br><br>
            
                <h3>1. Account Registration:</h3><br>
                <ul>
                <li>To access certain features of BINGO, you may need to create an account. When registering, you agree to provide accurate and complete information.</li>
                <li>You are responsible for maintaining the confidentiality of your account credentials and for all activities that occur under your account.</li>
                </ul><br>
            
                <h3>2. App Usage:</h3><br>
                <ul>
                <li>BINGO is designed to promote responsible waste disposal practices and community engagement in waste management initiatives.</li>
                <li>Users must not use BINGO for any unlawful, harmful, or inappropriate purposes. This includes but is not limited to: posting offensive content, engaging in illegal activities, or violating the rights of others.</li>
                <li>Users are encouraged to engage respectfully with other community members and contribute constructively to discussions.</li>
                </ul><br>
            
                <h3>3. Content Guidelines:</h3><br>
                <ul>
                <li>Users are responsible for the content they post on BINGO. Content must not violate any laws or infringe upon the rights of others.</li>
                <li>Content that promotes hate speech, violence, discrimination, or illegal activities is strictly prohibited.</li>
                <li>BINGO reserves the right to remove any content that violates these guidelines without prior notice.</li>
                </ul><br>
            
                <h3>4. Privacy and Data Security:</h3><br>
                <ul>
                <li>BINGO respects your privacy and is committed to protecting your personal information. Please refer to our Privacy Policy for details on how we collect, use, and disclose your data.</li>
                <li>You agree not to misuse any personal information of other users obtained through BINGO.</li>
                </ul><br>
            
                <h3>5. Reporting Violations:</h3><br>
                <ul>
                <li>If you encounter any violations of this user policy or observe inappropriate behavior on BINGO, please report it to us immediately.</li>
                <li>BINGO will investigate reported violations and take appropriate action, including removing offensive content and suspending or terminating accounts.</li>
                </ul><br>
            
                <h3>6. Changes to Policy:</h3><br>
                <ul>
                <li>BINGO reserves the right to update or modify this user policy at any time without prior notice. Any changes will be effective immediately upon posting.</li>
                <li>It is your responsibility to review this policy periodically to stay informed of any updates.</li>
                </ul><br><br>
            
                Thank you for being part of the BINGO community! If you have any questions or concerns about this user policy, please contact us at <a href='mailto:bingo@gmail.com'>bingo@gmail.com</a>.
            
            """.trimIndent()

        val spannableConverter = AddOnSpannableTextStyle()

        val spanned = spannableConverter.convertHtmlWithOrderedList(userPolicyText )

        binding.textViewUserPolicy.text = spanned
        val typeface = Typeface.DEFAULT
        binding.textViewUserPolicy.typeface = typeface
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}