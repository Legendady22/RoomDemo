package com.example.roomdemo

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomdemo.databinding.ActivityMainBinding
import com.example.roomdemo.databinding.DialogEditBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val employeeDAO = (application as EmployeeApp).db.employeeDao()
        binding?.btnAddRecord?.setOnClickListener {
            addRecord(employeeDAO)
        }

        lifecycleScope.launch {
            employeeDAO.fetchAllEmployees().collect {
                val list = ArrayList(it)
                setupListOfDataIntoRecyclerView(list, employeeDAO)
            }
        }
    }

    private fun addRecord(employeeDAO: EmployeeDAO){
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmail?.text.toString()

        if(name.isNotEmpty() && email.isNotEmpty()){

            lifecycleScope.launch {
                employeeDAO.insert(EmployeeEntity(name=name, email=email))
                Toast.makeText(applicationContext, "Record saved.", Toast.LENGTH_LONG).show()
                binding?.etName?.text?.clear()
                binding?.etEmail?.text?.clear()

            }
        }else{
            Toast.makeText(
                applicationContext,
                "Name or Email cannot be blank",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupListOfDataIntoRecyclerView(employeesList:ArrayList<EmployeeEntity>, employeeDao:EmployeeDAO){
        if(employeesList.isNotEmpty()){
            val itemAdapter = ItemAdapter(employeesList,
                {
                    updateId ->
                    updateRecordDialog(updateId, employeeDao)
                },
                {
                    deleteId ->
                    deleteRecordAlertDialog(deleteId, employeeDao)
                }
            )

            binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE


        }else{
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun updateRecordDialog(id:Int, employeeDao: EmployeeDAO){
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogEditBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDao.fetchEmployeeById(id).collect{
                if(it != null){
                    binding?.etEditName.setText(it.name)
                    binding?.etEditEmail.setText(it.email)
                }

            }
        }

        binding.tvUpdate.setOnClickListener {
            val name = binding.etEditName.text.toString()
            val email = binding.etEditEmail.text.toString()

            if(name.isNotEmpty() && email.isNotEmpty()){
                lifecycleScope.launch {
                    employeeDao.update(EmployeeEntity(id, name, email))
                    Toast.makeText(applicationContext, "Record Updated.",
                    Toast.LENGTH_LONG).show()

                    updateDialog.dismiss()
                }
            }else{
                Toast.makeText(applicationContext, "Name or Email cannot be blank.",
                Toast.LENGTH_LONG).show()
            }
        }
        binding.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    private fun deleteRecordAlertDialog(id:Int, employeeDao: EmployeeDAO){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){dialogInterface, _->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(applicationContext, "Record deleted successfully.",
                Toast.LENGTH_LONG).show()
            }
            dialogInterface.dismiss()

        }
        builder.setNegativeButton("No"){dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

}