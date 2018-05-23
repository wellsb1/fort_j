/*
 * Copyright 2008-2017 Wells Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.forty11.j.it;

import java.util.Iterator;

/**
 * Utility base class to make it easier to program simple typed 
 * iterator/iterables. 
 *  
 * @author Wells Burke
 */
public abstract class It<E> implements Iterator<E>, Iterable<E>
{
   E       next    = null;
   boolean started = false;

   protected abstract E findNext();

   public Iterator<E> iterator()
   {
      if (!started)
      {
         return this;
      }
      else
      {
         throw new UnsupportedOperationException("Can't call iterator() after hasNext() has been called");
      }
   }

   public boolean hasNext()
   {
      started = true;
      if (next == null)
      {
         next = findNext();
      }
      return next != null;
   }

   public E next()
   {
      E temp = next;
      next = null;
      return temp;
   }

   public void remove()
   {
      throw new UnsupportedOperationException("remove() is not supported.  Subclasses should provide necessary implementation");
   }

}
