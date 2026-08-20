package ru.alexandrros.petly.presentation.users

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.model.ReviewType
import ru.alexandrros.petly.domain.model.UserRating
import ru.alexandrros.petly.presentation.common.components.DetailRow
import ru.alexandrros.petly.presentation.common.components.OutlinedFilterChip
import ru.alexandrros.petly.presentation.common.components.SectionCard
import ru.alexandrros.petly.presentation.common.components.rememberContentInsets
import ru.alexandrros.petly.presentation.common.theme.ReviewStarColor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: String,
    onBackClick: () -> Unit,
    viewModel: UserDetailViewModel = koinViewModel(
        parameters = { parametersOf(userId) }
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    val contentInsets = rememberContentInsets()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        contentWindowInsets = contentInsets,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.user?.name?.ifEmpty { "Профиль" } ?: "Профиль",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (uiState.user != null && uiState.user!!.uid != uiState.currentUserId) {
                FloatingActionButton(
                    onClick = { viewModel.showAddReviewForm() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить отзыв")
                }
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage ?: "Неизвестная ошибка",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Пожалуйста, попробуйте позже",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.user != null -> {
                val user = uiState.user!!
                val isSpecialist = user.specialist != null

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isLandscape) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Max),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    Surface(
                                        modifier = Modifier.size(120.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        if (user.photoBytes != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(user.photoBytes)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Фото пользователя",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(64.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = user.name.ifEmpty { user.email },
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = user.specialist ?: "Не является специалистом",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSpecialist)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    SectionCard(title = "Информация") {
                                        DetailRow("Email", user.email)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    RatingSection(rating = uiState.rating)
                                }
                            }
                        }
                    } else {
                        item {
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                if (user.photoBytes != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(user.photoBytes)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Фото пользователя",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = user.name.ifEmpty { user.email },
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = user.specialist ?: "Не является специалистом",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSpecialist)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        item {
                            SectionCard(title = "Информация") {
                                DetailRow("Email", user.email)
                            }
                        }
                        item {
                            RatingSection(rating = uiState.rating)
                        }
                    }

                    item {
                        SectionCard(title = "Отзывы") {
                            if (isSpecialist) {
                                ReviewTypeSelector(
                                    selectedType = uiState.selectedReviewType,
                                    onTypeSelected = { type ->
                                        viewModel.setReviewType(type)
                                    }
                                )
                            } else {
                                Text(
                                    text = "Как владелец питомца",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    val filteredReviews = uiState.reviews.filter { it.type == uiState.selectedReviewType }
                    if (filteredReviews.isEmpty()) {
                        item {
                            Text(
                                text = "Отзывов пока нет",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(filteredReviews, key = { it.id }) { review ->
                            val canEdit = review.reviewerId == uiState.currentUserId
                            val canDelete = review.reviewerId == uiState.currentUserId
                            ReviewItem(
                                review = review,
                                canEdit = canEdit,
                                canDelete = canDelete,
                                onEdit = { viewModel.showEditReviewForm(review) },
                                onDelete = { viewModel.deleteReview(review.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.isReviewFormVisible) {
        val canSelectType = uiState.user?.specialist != null
        ReviewFormDialog(
            rating = uiState.reviewFormRating,
            comment = uiState.reviewFormComment,
            selectedType = uiState.reviewFormType,
            canSelectType = canSelectType,
            isEditing = uiState.editingReviewId != null,
            onRatingChange = viewModel::updateReviewFormRating,
            onCommentChange = viewModel::updateReviewFormComment,
            onTypeChange = viewModel::updateReviewFormType,
            onDismiss = viewModel::hideReviewForm,
            onSubmit = viewModel::submitReview
        )
    }
}

@Composable
private fun RatingSection(rating: UserRating?) {
    SectionCard(title = "Рейтинг") {
        if (rating == null || rating.totalCount == 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Нет оценок",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Рейтинг появится после первых отзывов",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            val fullStars = rating.average.toInt()
            val hasHalfStar = rating.average - fullStars >= 0.5

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(fullStars) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = ReviewStarColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (hasHalfStar) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = ReviewStarColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format(Locale.US, "%.1f", rating.average),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${rating.totalCount})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReviewTypeSelector(
    selectedType: ReviewType,
    onTypeSelected: (ReviewType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedFilterChip(
            text = "Как специалист",
            selected = selectedType == ReviewType.AS_SPECIALIST,
            onClick = { onTypeSelected(ReviewType.AS_SPECIALIST) },
            modifier = Modifier.weight(1f),
            labelModifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        OutlinedFilterChip(
            text = "Как владелец",
            selected = selectedType == ReviewType.AS_OWNER,
            onClick = { onTypeSelected(ReviewType.AS_OWNER) },
            modifier = Modifier.weight(1f),
            labelModifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReviewItem(
    review: Review,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = review.reviewerName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(review.rating) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = ReviewStarColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    repeat(5 - review.rating) {
                        Icon(
                            imageVector = Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (canEdit || canDelete) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canEdit) {
                        TextButton(
                            onClick = onEdit,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Редактировать")
                        }
                    }
                    if (canDelete) {
                        TextButton(
                            onClick = onDelete,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Удалить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewFormDialog(
    rating: Int,
    comment: String,
    selectedType: ReviewType,
    canSelectType: Boolean,
    isEditing: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onTypeChange: (ReviewType) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Изменить отзыв" else "Новый отзыв") },
        text = {
            Column {
                Text("Оценка")
                Row {
                    repeat(5) { index ->
                        IconButton(onClick = { onRatingChange(index + 1) }) {
                            Icon(
                                imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (index < rating) ReviewStarColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (canSelectType) {
                    ReviewTypeSelector(
                        selectedType = selectedType,
                        onTypeSelected = onTypeChange
                    )
                } else {
                    Text(
                        text = "Как владелец питомца",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    label = { Text("Комментарий") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSubmit,
                enabled = rating > 0 && comment.isNotBlank()
            ) {
                Text(if (isEditing) "Сохранить" else "Отправить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}