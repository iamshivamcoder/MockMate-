package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shivams.mockmate.model.UserProfile
import com.shivams.mockmate.ui.viewmodels.ProfileViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.shivams.mockmate.R
import com.shivams.mockmate.ui.util.AvatarUtils

// Define colors used in the screen
private val LightBlue = Color(0xFFE1F5FE)
private val DarkTeal = Color(0xFF00695C)
private val TextBlack = Color(0xFF1F1F1F)
private val AvatarBg = Color(0xFFB3E5FC)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()

    var name by remember(userProfile) { mutableStateOf(userProfile?.name ?: "") }
    var email by remember(userProfile) { mutableStateOf(userProfile?.email ?: "") }
    var phoneNumber by remember(userProfile) { mutableStateOf(userProfile?.phoneNumber ?: "") }
    var selectedAvatar by remember(userProfile) { mutableStateOf(userProfile?.avatar ?: "") }

    var isEditable by remember { mutableStateOf(false) }


    val avatarList = remember { AvatarUtils.AVATAR_MAP.keys.toList() }

    Scaffold(
        topBar = {
            CustomProfileTopAppBar(
                onBackClick = onNavigateBack,
                onEditClick = { isEditable = !isEditable },
                isEditing = isEditable
            )
        },
        bottomBar = {
            if (isEditable) {
                Button(
                    onClick = {
                        val updatedProfile = UserProfile(
                            name = name,
                            email = email,
                            phoneNumber = phoneNumber,
                            avatar = selectedAvatar
                        )
                        viewModel.saveUserProfile(updatedProfile)
                        isEditable = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkTeal,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(DarkTeal)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedAvatar.isNotEmpty()) {
                    val resId = AvatarUtils.getAvatarResId(selectedAvatar)
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Choose Your Avatar",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            AvatarGrid(
                avatars = avatarList,
                selectedIndex = avatarList.indexOf(selectedAvatar),
                onAvatarSelected = { index -> selectedAvatar = avatarList[index] }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Personal Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            PersonalDetailsForm(
                name = name,
                onNameChange = { name = it },
                email = email,
                onEmailChange = { email = it },
                phoneNumber = phoneNumber,
                onPhoneNumberChange = { phoneNumber = it },
                isEditable = isEditable
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomProfileTopAppBar(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    isEditing: Boolean
) {
    CenterAlignedTopAppBar(
        title = { 
            Text(
                text = "Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = TextBlack
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextBlack
                )
            }
        },
        actions = {
            IconButton(onClick = onEditClick) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.CheckCircle else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Save" else "Edit",
                        tint = TextBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEditing) "Save" else "Edit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextBlack
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = LightBlue
        )
    )
}

@Composable
fun AvatarGrid(
    avatars: List<String>,
    selectedIndex: Int,
    onAvatarSelected: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) 
    ) {
        itemsIndexed(avatars) { index, _ ->
            AvatarItem(
                resId = AvatarUtils.getAvatarResId(avatars[index]),
                isSelected = index == selectedIndex,
                onClick = { onAvatarSelected(index) }
            )
        }
    }
}

@Composable
fun AvatarItem(
    resId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AvatarBg)
            .then(
                if (isSelected) Modifier.border(
                    3.dp,
                    DarkTeal,
                    RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Avatar",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = DarkTeal,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

@Composable
fun PersonalDetailsForm(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    isEditable: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileTextField(label = "Name", value = name, onValueChange = onNameChange, isEditable = isEditable)
        ProfileTextField(
            label = "Email",
            value = email,
            onValueChange = onEmailChange,
            keyboardType = KeyboardType.Email,
            isEditable = isEditable
        )
        ProfileTextField(
            label = "Phone Number",
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            keyboardType = KeyboardType.Phone,
            isEditable = isEditable
        )
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isEditable: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DarkTeal,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = DarkTeal,
            cursorColor = DarkTeal
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        readOnly = !isEditable
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(onNavigateBack = {})
}
