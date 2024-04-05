package io.abood.messengerfacebood.model

data class Users (val name:String,val profilePic:String,val token:String){
    constructor():this("","","")
}