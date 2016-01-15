#include <mbgl/util/chrono.hpp>

namespace mbgl {

template Duration toDuration<Clock, Duration>(TimePoint);
template SystemDuration toDuration<SystemClock, SystemDuration>(SystemTimePoint);

template Seconds asSeconds<Duration>(Duration);
template Seconds asSeconds<Milliseconds>(Milliseconds);
template Seconds asSeconds<Microseconds>(Microseconds);

template Milliseconds asMilliseconds<Duration>(Duration);
template Milliseconds asMilliseconds<Seconds>(Seconds);
template Milliseconds asMilliseconds<Microseconds>(Microseconds);

template Microseconds asMicroseconds<Duration>(Duration);
template Microseconds asMicroseconds<Seconds>(Seconds);
template Microseconds asMicroseconds<Milliseconds>(Milliseconds);

template Seconds toSeconds<Clock, Duration>(TimePoint);
template Seconds toSeconds<SystemClock, SystemDuration>(SystemTimePoint);

template Milliseconds toMilliseconds<Clock, Duration>(TimePoint);
template Milliseconds toMilliseconds<SystemClock, SystemDuration>(SystemTimePoint);

template Microseconds toMicroseconds<Clock, Duration>(TimePoint);
template Microseconds toMicroseconds<SystemClock, SystemDuration>(SystemTimePoint);

} // namespace mbgl
