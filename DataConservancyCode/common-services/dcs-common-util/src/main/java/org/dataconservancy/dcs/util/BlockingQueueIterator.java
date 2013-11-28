/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;

/**
 * An iterator that operates off of a blocking queue.  Even though Queue implements Iterator, we need
 * to check for the queue poisoner, indicating the end of the queue.  Note that this iterator will block
 * in the <code>hasNext()</code> and <code>next()</code> methods if the queue is empty.  Once the poison
 * object is read from the queue, <code>hasNext()</code> returns <code>false</code> and <code>next()</code> will
 * throw <code>NoSuchElementException</code>.
 */
public class BlockingQueueIterator<T> implements Iterator<T> {
    private final BlockingQueue<T> queue;
    private final T poison;

    private T current;
    private boolean releasedCurrent;

    public BlockingQueueIterator(BlockingQueue<T> queue, T poison) {
        if (queue == null) {
            throw new IllegalArgumentException("Queue must not be null.");
        }
        if (poison == null) {
            throw new IllegalArgumentException("Poison must not be null.");
        }

        this.queue = queue;
        this.poison = poison;
    }

    @Override
    public boolean hasNext() {
        if (releasedCurrent || current == null) {
            try {
                current = queue.take();
                releasedCurrent = false;
            } catch (InterruptedException e) {
                // restore interrupted status
                Thread.currentThread().interrupt();
            }
        }

        return poison != current;
    }

    @Override
    public T next() {

        if (poison == current) {
            throw new NoSuchElementException();
        }

        if (!releasedCurrent && current != null) {
            releasedCurrent = true;
            return current;
        }

        try {
            current = queue.take();
            if (poison == current) {
                throw new NoSuchElementException();
            }
            releasedCurrent = true;
            return current;
        } catch (InterruptedException e) {
            // pretend we are empty
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Operation not supported.");
    }
}
