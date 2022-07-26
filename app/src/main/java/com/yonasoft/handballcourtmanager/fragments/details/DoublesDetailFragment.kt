package com.yonasoft.handballcourtmanager.fragments.details

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yonasoft.handballcourtmanager.R
import com.yonasoft.handballcourtmanager.databinding.FragmentDoublesDetailBinding
import com.yonasoft.handballcourtmanager.db.matchesdb.MatchType
import com.yonasoft.handballcourtmanager.fragments.details.dialogs.EndMatchDialogFragment
import com.yonasoft.handballcourtmanager.fragments.details.viewmodel.MatchDetailViewModel

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

//Doubles detail fragment when a doubles match is pressed in the matches fragment
@AndroidEntryPoint
class DoublesDetailFragment : Fragment() {

    private var binding: FragmentDoublesDetailBinding? = null
    private val args: DoublesDetailFragmentArgs by navArgs()
    private val matchDetailViewModel:MatchDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_doubles_detail, container, false)
        val view = binding!!.root
        binding!!.viewModel = matchDetailViewModel
        matchDetailViewModel
        setupObservers()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Setup click listeners change focus listeners etc.
        setUpListeners()
    }

    private fun setUpListeners() {
        binding!!.apply {
            //Add or change players when click which open an add player dialog
            tvT1P1.setOnClickListener {
                changeOrAddPlayer("t1p1")
            }

            tvT1P2.setOnClickListener {
                changeOrAddPlayer("t1p2")
            }

            tvT2P1.setOnClickListener {
                changeOrAddPlayer("t2p1")
            }

            tvT2P2.setOnClickListener {
                changeOrAddPlayer("t2p2")
            }

            //Add or deduct points based on team
            btnT1Add.setOnClickListener {
                matchDetailViewModel.addPoints("t1")
            }
            btnT2Add.setOnClickListener {
                matchDetailViewModel.addPoints("t2")
            }
            btnT1Sub.setOnClickListener {
                matchDetailViewModel.deductPoints("t1")
            }
            btnT2Sub.setOnClickListener {
                matchDetailViewModel.deductPoints("t2")
            }

            //Ends match
            btnEndMatch.setOnClickListener {
                val match = matchDetailViewModel.match.value!!
                //Makes sure all the textview for the players are filled in
                if (match.teams[1]!![0]!= "TBA" && match.teams[2]!![0] != "TBA" && match.teams[1]!![1] != "TBA" && match.teams[2]!![1] != "TBA") {
                    //Navigates to end match confirmation
                    findNavController().navigate(
                        DoublesDetailFragmentDirections.actionFragmentDoublesDetailToEndMatchDialogFragment()
                    )
                    //Set listener to determine the result of the dialog.
                    setFragmentResultListener(EndMatchDialogFragment.REQUEST_KEY_END) { _, bundle ->
                        val result = bundle.getBoolean(EndMatchDialogFragment.BUNDLE_KEY_END)
                        //If the answer is yes to the dialog this runs
                        if (result) {
                            //Pop back stack to this fragment
                            findNavController().popBackStack()
                            //Navigate to dialog to return to winner's queue with an array of the players in this match, and the match type
                            findNavController().navigate(
                                DoublesDetailFragmentDirections.actionFragmentDoublesDetailToReturnToWinnersDialogFragment(
                                    arrayOf(
                                        match.teams[1]!![0],
                                        match.teams[1]!![1],
                                        match.teams[2]!![0],
                                        match.teams[2]!![1],
                                        match.teams[3]!![0]
                                    ),
                                    MatchType.DOUBLES.name
                                )
                            )
                            //Completes match
                            matchDetailViewModel.completeMatch()
                        }

                    }
                } else {
                    //Prompts user to add player if they haven't already
                    Toast.makeText(context, "Press the \"TBA\" to add a player!", Toast.LENGTH_LONG)
                        .show()
                }
            }
            //Code for interaction for editing court number
            editTextNum.apply {
                //Clears the court number for future editing
                setOnClickListener {
                    binding!!.editTextNum.text!!.clear()
                }
                //When you press the check button aka enter? on the on-screen keyboard it will set the new edited text as the court number
                setImeActionLabel(binding!!.editTextNum.text.toString(), KeyEvent.KEYCODE_ENTER)
                //Changes court number when out of focus
                setOnFocusChangeListener { _, _ ->
                    matchDetailViewModel.updateCourtNum(binding!!.editTextNum.text.toString())
                    isCursorVisible=false
                }
            }
        }
    }

    //Observers for updated data to be reflected in the views
    private fun setupObservers() {
        binding!!.viewModel!!.match.observe(viewLifecycleOwner) {
            binding!!.apply {
                tvT1P1.text = it.teams[1]!![0]
                tvT1P2.text = it.teams[1]!![1]
                tvT2P1.text = it.teams[2]!![0]
                tvT2P2.text = it.teams[2]!![1]
                tvT1Score.text = it.scores[1].toString()
                tvT2Score.text = it.scores[2].toString()
            }
        }
    }


    //Code for changing and adding player
    //Parameter is what player and team will be changed in this match represented as a string
    private fun changeOrAddPlayer(playerAndTeam: String) {
        //Navigates to player selection screen
        findNavController().navigate(
            DoublesDetailFragmentDirections.actionFragmentDoublesDetailToSelectFromRosterFragment(
            )
        )
        //Gets the result of the player that will be added or changed to the match from the selection fragment that was opened
        setFragmentResultListener(SelectFromRosterFragment.REQUEST_KEY_PLAYER) { _, bundle ->
            val result = bundle.getString(SelectFromRosterFragment.BUNDLE_KEY_PLAYER)
            when (playerAndTeam) {
                //t for team and p for player
                "t1p1" -> matchDetailViewModel.match.value!!.teams[1]!![0] = result!!
                "t1p2" -> matchDetailViewModel.match.value!!.teams[1]!![1]= result!!
                "t2p1" -> matchDetailViewModel.match.value!!.teams[2]!![0] = result!!
                "t2p2" -> matchDetailViewModel.match.value!!.teams[2]!![1]= result!!
            }
            //Updates the players in tha match through the view model into database
            matchDetailViewModel.updateMatch()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}