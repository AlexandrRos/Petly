package ru.alexandrros.petly.presentation.users

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import ru.alexandrros.petly.presentation.common.isSpecialistValue
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
                val isSpecialist = user.specialist.isSpecialistValue()

                // Non-specialists can only have AS_OWNER reviews
                val displayReviewType = if (isSpecialist) {
                    uiState.selectedReviewType
                } else {
                    ReviewType.AS_OWNER
                }

                // LazyListState and pending scroll index for smooth scrolling to forms
                val listState = rememberLazyListState()
                var pendingScrollIndex by remember { mutableStateOf<Int?>(null) }

                // Trigger smooth scroll when the review form appears (add or edit)
                LaunchedEffect(uiState.isReviewFormVisible) {
                    if (uiState.isReviewFormVisible) {
                        pendingScrollIndex?.let { index ->
                            listState.animateScrollToItem(index)
                            pendingScrollIndex = null
                        }
                    }
                }

                // All reviews of the displayed type (no rating filter)
                val allReviewsOfType = uiState.reviews.filter { it.type == displayReviewType }

                // Current user's review is always shown, regardless of filters
                val currentUserReviewOfType = allReviewsOfType.firstOrNull {
                    it.reviewerId == uiState.currentUserId
                }

                // Other reviews: apply rating filter + sort
                val otherReviews = allReviewsOfType
                    .filter { it.id != currentUserReviewOfType?.id }
                    .filter { review ->
                        review.rating.toFloat() >= uiState.ratingFilterMin &&
                                review.rating.toFloat() <= uiState.ratingFilterMax
                    }
                    .sortedWith(
                        when (uiState.reviewSortOption) {
                            ReviewSortOption.LATEST -> compareByDescending { it.timestamp }
                            ReviewSortOption.OLDEST -> compareBy { it.timestamp }
                            ReviewSortOption.HIGHEST_RATING -> compareByDescending { it.rating }
                            ReviewSortOption.LOWEST_RATING -> compareBy { it.rating }
                        }
                    )

                LazyColumn(
                    state = listState,
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
                                        text = if (isSpecialist) user.specialist ?: "" else "Не является специалистом",
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
                                text = if (isSpecialist) user.specialist ?: "" else "Не является специалистом",
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
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Отзывы",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = { viewModel.showReviewFilterDialog() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = "Фильтры отзывов",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Review type selector or non‑specialist label
                                if (isSpecialist) {
                                    ReviewTypeSelector(
                                        selectedType = displayReviewType,
                                        onTypeSelected = { type -> viewModel.setReviewType(type) }
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
                    }

                    if (uiState.isCurrentUserLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    } else {
                        // Prevent self-review: only show form if viewing another user
                        if (uiState.isReviewFormVisible &&
                            uiState.editingReviewId == null &&
                            user.uid != uiState.currentUserId
                        ) {
                            item {
                                ReviewFormCard(
                                    rating = uiState.reviewFormRating,
                                    comment = uiState.reviewFormComment,
                                    selectedType = displayReviewType,
                                    isEditing = false,
                                    isSubmitting = uiState.isSubmittingReview,
                                    onRatingChange = viewModel::updateReviewFormRating,
                                    onCommentChange = viewModel::updateReviewFormComment,
                                    onCancel = viewModel::hideReviewForm,
                                    onSubmit = viewModel::submitReview
                                )
                            }
                        }

                        // If there is an existing review and form is not editing it, show it (or form if editing)
                        if (currentUserReviewOfType != null) {
                            val isEditingThisReview = uiState.isReviewFormVisible &&
                                    uiState.editingReviewId == currentUserReviewOfType.id
                            if (isEditingThisReview && user.uid != uiState.currentUserId) {
                                item {
                                    ReviewFormCard(
                                        rating = uiState.reviewFormRating,
                                        comment = uiState.reviewFormComment,
                                        selectedType = displayReviewType,
                                        isEditing = true,
                                        isSubmitting = uiState.isSubmittingReview,
                                        onRatingChange = viewModel::updateReviewFormRating,
                                        onCommentChange = viewModel::updateReviewFormComment,
                                        onCancel = viewModel::hideReviewForm,
                                        onSubmit = viewModel::submitReview
                                    )
                                }
                            } else {
                                item(key = currentUserReviewOfType.id) {
                                    ReviewItem(
                                        review = currentUserReviewOfType,
                                        canEdit = user.uid != uiState.currentUserId,
                                        canDelete = user.uid != uiState.currentUserId,
                                        isCurrentUser = true,
                                        isDeleting = currentUserReviewOfType.id in uiState.deletingReviewIds,
                                        onEdit = {
                                            if (user.uid != uiState.currentUserId) {
                                                // Capture the index of this review before switching to edit form
                                                val index = listState.layoutInfo.visibleItemsInfo
                                                    .firstOrNull { it.key == currentUserReviewOfType.id }
                                                    ?.index
                                                pendingScrollIndex = index
                                                viewModel.showEditReviewForm(currentUserReviewOfType)
                                            }
                                        },
                                        onDelete = {
                                            if (user.uid != uiState.currentUserId) {
                                                viewModel.deleteReview(currentUserReviewOfType.id)
                                            }
                                        }
                                    )
                                }
                            }
                        } else if (!uiState.isReviewFormVisible && user.uid != uiState.currentUserId) {
                            // No review and no form: placeholder with add button
                            item(key = "add_review_placeholder") {
                                AddReviewPlaceholder(
                                    onClick = {
                                        val index = listState.layoutInfo.visibleItemsInfo
                                            .firstOrNull { it.key == "add_review_placeholder" }
                                            ?.index
                                        pendingScrollIndex = index
                                        viewModel.showAddReviewForm()
                                    }
                                )
                            }
                        }
                    }

                    items(otherReviews, key = { it.id }) { review ->
                        ReviewItem(
                            review = review,
                            canEdit = false,
                            canDelete = false,
                            isCurrentUser = false,
                            isDeleting = false,
                            onEdit = {},
                            onDelete = {}
                        )
                    }
                }
            }
        }
    }

    if (uiState.isReviewFilterDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.hideReviewFilterDialog() },
            title = { Text("Фильтры отзывов") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Сортировка:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(100.dp)
                        )
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            var sortMenuExpanded by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { sortMenuExpanded = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (uiState.reviewSortOption) {
                                        ReviewSortOption.LATEST -> "Сначала новые"
                                        ReviewSortOption.OLDEST -> "Сначала старые"
                                        ReviewSortOption.HIGHEST_RATING -> "С высоким рейтингом"
                                        ReviewSortOption.LOWEST_RATING -> "С низким рейтингом"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                ReviewSortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                when (option) {
                                                    ReviewSortOption.LATEST -> "Сначала новые"
                                                    ReviewSortOption.OLDEST -> "Сначала старые"
                                                    ReviewSortOption.HIGHEST_RATING -> "С высоким рейтингом"
                                                    ReviewSortOption.LOWEST_RATING -> "С низким рейтингом"
                                                }
                                            )
                                        },
                                        onClick = {
                                            viewModel.setReviewSortOption(option)
                                            sortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text("Диапазон рейтинга", style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Мин: ${uiState.ratingFilterMin.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(60.dp)
                        )
                        Slider(
                            value = uiState.ratingFilterMin,
                            onValueChange = { viewModel.setRatingFilterMin(it) },
                            valueRange = 0f..5f,
                            steps = 4,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Макс: ${uiState.ratingFilterMax.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(60.dp)
                        )
                        Slider(
                            value = uiState.ratingFilterMax,
                            onValueChange = { viewModel.setRatingFilterMax(it) },
                            valueRange = 0f..5f,
                            steps = 4,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideReviewFilterDialog() }) {
                    Text("Применить")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.resetReviewFilters() }) {
                        Text("Сбросить")
                    }
                    TextButton(onClick = { viewModel.hideReviewFilterDialog() }) {
                        Text("Отмена")
                    }
                }
            }
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
    isCurrentUser: Boolean,
    isDeleting: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isCurrentUser) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        tonalElevation = 2.dp,
        border = if (isCurrentUser) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isCurrentUser) "${review.reviewerName} (Ваш отзыв)" else review.reviewerName,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                            enabled = !isDeleting,  // Prevent editing while deleting
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
                            enabled = !isDeleting,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
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
}

@Composable
private fun AddReviewPlaceholder(onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Вы ещё не оставили отзыв этого типа",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Написать отзыв")
            }
        }
    }
}

@Composable
private fun ReviewFormCard(
    rating: Int,
    comment: String,
    selectedType: ReviewType,
    isEditing: Boolean,
    isSubmitting: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isEditing) "Изменить отзыв" else "Новый отзыв",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (selectedType == ReviewType.AS_SPECIALIST) "Тип: Как специалист" else "Тип: Как владелец",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Оценка", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(8.dp))
                repeat(5) { index ->
                    IconButton(onClick = { onRatingChange(index + 1) }) {
                        Icon(
                            imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (index < rating) ReviewStarColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                label = { Text("Комментарий") },
                placeholder = { Text("Введите ваш отзыв") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text("Отмена")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSubmit,
                    enabled = rating > 0 && comment.isNotBlank() && !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (isEditing) "Сохранить" else "Отправить")
                    }
                }
            }
        }
    }
}