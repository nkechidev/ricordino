package com.ricordino.di

import com.ricordino.pipeline.CategoryClassifier
import com.ricordino.pipeline.KeywordCategoryClassifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PipelineModule {
    // v2 swaps this single binding for LlmCategoryClassifier — no other code changes.
    @Binds
    abstract fun bindCategoryClassifier(impl: KeywordCategoryClassifier): CategoryClassifier
}
