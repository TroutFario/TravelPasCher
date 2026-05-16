package fr.troubidoo.travelpascher.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme

@Composable
fun ChoiceContent(onLoginClick: () -> Unit, onSignupClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(stringResource(R.string.login_button))
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onSignupClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(stringResource(R.string.signup_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChoiceContentPreview() {
    TravelPasCherTheme {
        ChoiceContent(onLoginClick = {}, onSignupClick = {})
    }
}
