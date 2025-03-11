package com.example.fitness_tracking_app.features

import com.example.fitness_tracking_app.R

enum class MotivationOption(val key: String, val titleId: Int, val arrayId: Int) {
    FOR_FUN("FOR_FUN", R.string.just_for_fun, R.array.for_fun_texts),
    WEIGHT_LOSS("WEIGHT LOSS", R.string.to_lose_some_weight, R.array.weight_loss_texts),
    PHYSICAL_CONDITION("PHYSICAL_CONDITION", R.string.to_improve_my_physical_condition, R.array.physical_condition_texts),
    HEALTH("HEALTH", R.string.to_improve_my_health, R.array.health_texts),
    NONE("NONE", R.string.i_dont_want_any_motivational_text, 0),
    CUSTOM("CUSTOM", R.string.i_want_to_set_my_own_motivational_text, 0);

    companion object {
        fun fromKey(key: String?): MotivationOption {
            return values().find { it.key == key} ?: NONE
        }
    }
}