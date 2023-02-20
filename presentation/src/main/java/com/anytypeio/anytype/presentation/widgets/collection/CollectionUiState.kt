package com.anytypeio.anytype.presentation.widgets.collection

import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.presentation.objects.ObjectAction

class CollectionUiState(
    val views: Resultat<List<CollectionView>>,
    val showEditMode: Boolean,
    val showWidget: Boolean,
    val collectionName: String,
    val actionName: String,
    val objectActions: List<ObjectAction>,
    val showBurgerMenu: Boolean,
    val displayType: Boolean,
)