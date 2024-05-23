package com.hits.graphic_editor.utils

import android.widget.ImageView
import com.hits.graphic_editor.custom_api.MipMapsContainer
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import kotlin.math.min
fun <T> MutableList<T>.shrinkSize(size: Int)
{
    while (this.size > size)
        this.removeLast()
}
class ProcessedImage(
    image: SimpleImage,
    private var imageView: ImageView
) {
    companion object {
        const val MAX_SIZE = 7680* 4320
        const val MIN_SIZE = 10 * 10
    }

    private var mipMapsContainer: MipMapsContainer = MipMapsContainer(image)

    private var globalStack: MutableList<SimpleImage> = mutableListOf(image)
    private var localStack: MutableList<SimpleImage> = mutableListOf()
    private var globalStackIndex: Int = 0
    private var localStackIndex: Int = 0
    private var isInGlobalStack = true

    private var maxLocalStackSize = 1
    private var maxGlobalStackSize = 5
    /**
     * Сразу после вызываем 'getSimpleImage' для получения текущего изображения
     */
    fun undo() {
        if (isInGlobalStack){
            if (globalStackIndex == 0)
                return
            globalStackIndex--
            mipMapsContainer.cancelJobs()
            mipMapsContainer = MipMapsContainer(globalStack[globalStackIndex])
        }
        else localStackIndex = (localStackIndex - 1).coerceAtLeast(0)
    }
    /**
     * Скорее всего пригодится только это
     */
    fun undoAndSetImageToView(){
        undo()
        imageView.setImageBitmap(getBitMap(this.getSimpleImage()))
    }
    /**
     * Сразу после вызываем 'getSimpleImage' для получения текущего изображения
     */
    fun redo() {
        if (isInGlobalStack){
            if (globalStackIndex == globalStack.size - 1)
                return
            globalStackIndex++
            mipMapsContainer.cancelJobs()
            mipMapsContainer = MipMapsContainer(globalStack[globalStackIndex])
        }
        else localStackIndex = min(localStack.size - 1, localStackIndex + 1)
    }
    /**
     * Скорее всего пригодится только это
     */
    fun redoAndSetImageToView(){
        redo()
        imageView.setImageBitmap(getBitMap(this.getSimpleImage()))
    }
    fun getSimpleImage(): SimpleImage{
        if (isInGlobalStack) return globalStack[globalStackIndex]
        return localStack[localStackIndex]
    }
    fun getSimpleImageBeforeFiltering(): SimpleImage{
        return globalStack[globalStackIndex]
    }
    /**
     * Вызываем каждый раз при переходе между окном фильра и общим окном
     * Если вызывается при выходе из фильтра через галочку, то вызываем с параметром 'changesApplied = true'
     * Хендлит отсутсвие изменений при выходе на галочку
     */
    fun switchStackMode(changesApplied: Boolean = false) {
        if (!isInGlobalStack && changesApplied &&
            localStack[localStackIndex] != globalStack[globalStackIndex])
        {
            globalStack.shrinkSize(globalStackIndex + 1)
            globalStack.add(localStack[localStackIndex])
            if (globalStack.size > maxGlobalStackSize)
                globalStack.removeFirst()
            globalStackIndex = globalStack.size - 1
            mipMapsContainer.cancelJobs()
            mipMapsContainer = MipMapsContainer(globalStack[globalStackIndex])
        }
        localStack.clear()
        localStackIndex = 0

        if (isInGlobalStack)
            localStack.add(globalStack[globalStackIndex])


        isInGlobalStack = !isInGlobalStack
    }
    /**
     * Вызывается только внутри локального стека,
     * Автоматически переходит на добавленную картинку (те после надо вызвать 'getSimpleImage')
     */
    fun addToLocalStack(img: SimpleImage) {
        if (isInGlobalStack) return

        localStack.shrinkSize(localStackIndex + 1)
        localStack.add(img)
        if (localStack.size > maxLocalStackSize)
            localStack.removeFirst()
        localStackIndex = localStack.size - 1
    }

    fun getMipMapsContainer(): MipMapsContainer{
        return this.mipMapsContainer
    }
    fun setImageToView() {
        imageView.setImageBitmap(getBitMap(getSimpleImage()))
    }
    fun addToLocalStackAndSetImageToView(img: SimpleImage){
        addToLocalStack(img)
        setImageToView()
    }
}