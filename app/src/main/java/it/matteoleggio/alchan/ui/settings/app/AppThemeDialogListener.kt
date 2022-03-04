package it.matteoleggio.alchan.ui.settings.app

import it.matteoleggio.alchan.helper.enums.AppColorTheme

interface AppThemeDialogListener {
    fun passSelectedTheme(theme: AppColorTheme)
}