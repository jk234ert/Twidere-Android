/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.api.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 15/3/31.
 */
@ParcelablePlease
public class Indices implements Parcelable {

    int start, end;

    Indices() {

    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public Indices(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "Index{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        IndicesParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Indices> CREATOR = new Creator<Indices>() {
        @Override
        public Indices createFromParcel(Parcel source) {
            Indices target = new Indices();
            IndicesParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Indices[] newArray(int size) {
            return new Indices[size];
        }
    };
}
