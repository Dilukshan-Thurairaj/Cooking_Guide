package com.example.cw2_meal

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Meals")
data class MealDBEntity (
    @ColumnInfo(name = "mealID") val mealID: String,
    @PrimaryKey
    @ColumnInfo(name = "mealName") val mealName: String,
    @ColumnInfo(name = "drinkAlternate") val drink: String?,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "area") val area: String?,
    @ColumnInfo(name = "instruction") val instruction: String?,
    @ColumnInfo(name = "mealThumb") val mealThumb: String?,
    @ColumnInfo(name = "tags") val tag: String?,
    @ColumnInfo(name = "youtube") val youtube: String?,
    @ColumnInfo(name = "ingredients") val ingredient: String?,
    @ColumnInfo(name = "measures") val measure: String?,
    @ColumnInfo(name = "source") val source: String?,
    @ColumnInfo(name = "imageSource") val imageSource: String?,
    @ColumnInfo(name = "creativeCommonsConfirmed") val creativeCommons: String?,
    @ColumnInfo(name = "dateModified") val dateModified: String?,
        ): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(mealID)
        dest.writeString(mealName)
        dest.writeString(drink)
        dest.writeString(category)
        dest.writeString(area)
        dest.writeString(instruction)
        dest.writeString(mealThumb)
        dest.writeString(tag)
        dest.writeString(youtube)
        dest.writeString(ingredient)
        dest.writeString(measure)
        dest.writeString(source)
        dest.writeString(imageSource)
        dest.writeString(creativeCommons)
        dest.writeString(dateModified)
    }

    companion object CREATOR : Parcelable.Creator<MealDBEntity> {
        override fun createFromParcel(parcel: Parcel): MealDBEntity {
            return MealDBEntity(parcel)
        }

        override fun newArray(size: Int): Array<MealDBEntity?> {
            return arrayOfNulls(size)
        }
    }
}

